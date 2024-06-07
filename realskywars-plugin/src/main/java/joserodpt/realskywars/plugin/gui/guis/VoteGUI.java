package joserodpt.realskywars.plugin.gui.guis;

/*
 *   _____            _  _____ _
 *  |  __ \          | |/ ____| |
 *  | |__) |___  __ _| | (___ | | ___   ___      ____ _ _ __ ___
 *  |  _  // _ \/ _` | |\___ \| |/ / | | \ \ /\ / / _` | '__/ __|
 *  | | \ \  __/ (_| | |____) |   <| |_| |\ V  V / (_| | |  \__ \
 *  |_|  \_\___|\__,_|_|_____/|_|\_\\__, | \_/\_/ \__,_|_|  |___/
 *                                   __/ |
 *                                  |___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealSkywars
 */

import joserodpt.realskywars.api.RealSkywarsAPI;
import joserodpt.realskywars.api.map.RSWMap;
import joserodpt.realskywars.api.managers.LanguageManagerAPI;
import joserodpt.realskywars.api.player.RSWPlayer;
import joserodpt.realskywars.api.utils.Itens;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;

public class VoteGUI {

    private static Map<UUID, VoteGUI> inventories = new HashMap<>();
    private Inventory inv;
    private ItemStack close = Itens.createItem(Material.OAK_DOOR, 1, "&cClose", Collections.singletonList("&fClick here to close this menu."));
    private final UUID uuid;
    private final RSWPlayer p;

    public enum VoteSetting {CHESTS, TIME, PROJECTILE}

    private VoteSetting def = VoteSetting.CHESTS;

    public VoteGUI(RSWPlayer p) {
        this.p = p;
        this.uuid = p.getUUID();
        this.inv = Bukkit.getServer().createInventory(null, 54, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.MENU_VOTE_TITLE, false));

        fillChest();
    }

    public void fillChest() {
        this.inv.clear();

        //selection items
        this.inv.setItem(10, Itens.createItem(Material.CHEST, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.MENU_CHESTS_TITLE, false)));
        this.inv.setItem(19, Itens.createItem(Material.CLOCK, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.MENU_TIME_TITLE, false)));
        this.inv.setItem(28, Itens.createItem(Material.ARROW, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.MENU_PROJECTILES_TITLE, false)));

        this.inv.setItem(37, close);

        switch (def) {
            case CHESTS:
                this.inv.setItem(13, Itens.createItem(Material.WOODEN_SWORD, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(this.p, LanguageManagerAPI.TS.CHEST_BASIC, false)));
                this.inv.setItem(14, Itens.createItem(Material.CHEST, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CHEST_NORMAL, false)));
                this.inv.setItem(15, Itens.createItem(Material.ENDER_CHEST, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CHEST_EPIC, false)));
                break;
            case TIME:
                this.inv.setItem(22, Itens.createItem(Material.YELLOW_CONCRETE, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_DAY, false)));
                this.inv.setItem(23, Itens.createItem(Material.RED_CONCRETE, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_SUNSET, false)));
                this.inv.setItem(24, Itens.createItem(Material.BLACK_CONCRETE, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_NIGHT, false)));
                this.inv.setItem(25, Itens.createItem(Material.WATER_BUCKET, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_RAIN, false)));
                break;
            case PROJECTILE:
                this.inv.setItem(31, Itens.createItem(Material.EGG, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.PROJECTILE_NORMAL, false)));
                this.inv.setItem(33, Itens.createItem(Material.COBBLESTONE, 1, RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.PROJECTILE_BREAK, false)));
                break;
        }

        for (int number : new int[]{0, 1, 2, 9, 11, 18, 20, 27, 29, 36, 38, 45, 46, 47}) {
            this.inv.setItem(number, Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));
        }
    }

    public void openInventory(Player target) {
        Inventory inv = getInventory();
        InventoryView openInv = target.getOpenInventory();
        if (openInv != null) {
            Inventory openTop = target.getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                target.openInventory(inv);
            }
            register();
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        VoteGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);

                        RSWPlayer p = current.p;

                        switch (e.getRawSlot()) {
                            case 10:
                                current.def = VoteSetting.CHESTS;
                                current.fillChest();
                                break;
                            case 19:
                                current.def = VoteSetting.TIME;
                                current.fillChest();
                                break;
                            case 28:
                                current.def = VoteSetting.PROJECTILE;
                                current.fillChest();
                                break;

                            //Chest vote
                            case 13:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.CHESTS, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.basic")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.CHESTS, 1);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CHEST_BASIC, false)));
                                    } else {
                                        p.closeInventory();
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                    }
                                }
                                break;
                            case 14:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.CHESTS, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.normal")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.CHESTS, 2);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CHEST_NORMAL, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;
                            case 15:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.CHESTS, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.epic")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.CHESTS, 3);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CHEST_EPIC, false)));

                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;

                            //time vote

                            case 22:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.TIME, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.day")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.TIME, 1);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_DAY, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;

                            case 23:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.TIME, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.sunset")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.TIME, 2);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_SUNSET, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;
                            case 24:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.TIME, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.night")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.TIME, 3);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_NIGHT, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;
                            case 25:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.TIME, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.rain")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.TIME, 4);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.TIME_NIGHT, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;

                            //projectile vote
                            case 31:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.PROJECTILES, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.normal-projectile")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.PROJECTILES, 1);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.PROJECTILE_NORMAL, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;
                            case 33:
                                if (!p.isBot() && p.getMatch().hasVotedFor(RSWMap.VoteType.PROJECTILES, p.getUUID())) {
                                    p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.ALREADY_VOTED, true));
                                    p.closeInventory();
                                } else {
                                    if (p.getPlayer().hasPermission("rs.break-projectile")) {
                                        p.getMatch().addVote(p.getUUID(), RSWMap.VoteType.PROJECTILES, 2);
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.VOTE, true).replace("%thing%", RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.PROJECTILE_BREAK, false)));
                                    } else {
                                        p.sendMessage(RealSkywarsAPI.getInstance().getLanguageManagerAPI().getString(p, LanguageManagerAPI.TS.CMD_NOPERM, true));
                                        p.closeInventory();
                                    }
                                }
                                break;

                            case 37:
                                p.closeInventory();
                                break;
                        }
                    }
                }
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    public Inventory getInventory() {
        return inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }
}