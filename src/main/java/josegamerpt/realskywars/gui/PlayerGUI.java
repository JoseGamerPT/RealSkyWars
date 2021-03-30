package josegamerpt.realskywars.gui;

import josegamerpt.realskywars.classes.Enum;
import josegamerpt.realskywars.managers.LanguageManager;
import josegamerpt.realskywars.player.RSWPlayer;
import josegamerpt.realskywars.utils.Itens;
import josegamerpt.realskywars.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerGUI {

    private static Map<UUID, PlayerGUI> inventories = new HashMap<>();
    private static Map<UUID, Integer> refresh = new HashMap<>();
    static Inventory inv;
    static RSWPlayer gp;
    private UUID uuid;

    public PlayerGUI(RSWPlayer p, UUID id, RSWPlayer target) {
        this.uuid = id;
        gp = p;

        inv = Bukkit.getServer().createInventory(null, InventoryType.HOPPER, target.getName());

        // infoMap
        ArrayList<String> lore = new ArrayList<>();
        for (String s : LanguageManager.getList(p, Enum.TL.STATS_ITEM_LORE)) {
            lore.add(variables(s, target));
        }
        ItemStack infoMap = Itens.createItemLore(Material.MAP, 1, LanguageManager.getString(p, Enum.TS.STATS_ITEM_NAME, false).replace("%player%", target.getDisplayName()),
                lore);
        inv.setItem(2, infoMap);

        inventories.put(id, this);
    }

    protected String variables(String s, RSWPlayer gp) {
            return s.replace("%space%", Text.makeSpace()).replace("%kills%", gp.getStatistics(RSWPlayer.PlayerStatistics.TOTAL_KILLS) + "")
                    .replace("%coins%", gp.getCoins() + "").replace("%deaths%", gp.getStatistics(RSWPlayer.PlayerStatistics.DEATHS) + "").replace("%solowins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_SOLO) + "").replace("%teamwins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_TEAMS) + "").replace("%loses%", gp.getStatistics(RSWPlayer.PlayerStatistics.LOSES) + "").replace("%gamesplayed%", gp.getStatistics(RSWPlayer.PlayerStatistics.GAMES_PLAYED) + "");
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
                    Player p = (Player) clicker;
                    if (p != null) {
                        UUID uuid = p.getUniqueId();
                        if (inventories.containsKey(uuid)) {
                            PlayerGUI current = inventories.get(uuid);
                            if (!e.getInventory().getType().name()
                                    .equalsIgnoreCase(current.getInventory().getType().name())) {
                                return;
                            }
                            e.setCancelled(true);
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

                        if (refresh.containsKey(uuid)) {
                            Bukkit.getScheduler().cancelTask(refresh.get(uuid));
                        }
                    }
                }
            }
        };
    }

    public void openInventory(RSWPlayer player) {
        Inventory inv = getInventory();
        InventoryView openInv = player.getPlayer().getOpenInventory();
        if (openInv != null) {
            Inventory openTop = player.getPlayer().getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                player.getPlayer().openInventory(inv);
            }
            register();
        }
    }

    private Inventory getInventory() {
        return inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }
}
