package josegamerpt.realskywars.player;

import josegamerpt.realskywars.RealSkywars;
import josegamerpt.realskywars.cages.Cage;
import josegamerpt.realskywars.configuration.Config;
import josegamerpt.realskywars.configuration.Players;
import josegamerpt.realskywars.effects.BlockWinTrail;
import josegamerpt.realskywars.effects.Trail;
import josegamerpt.realskywars.managers.LanguageManager;
import josegamerpt.realskywars.kits.Kit;
import josegamerpt.realskywars.misc.Selections;
import josegamerpt.realskywars.misc.SetupRoom;
import josegamerpt.realskywars.misc.Team;
import josegamerpt.realskywars.game.modes.SWGameMode;
import josegamerpt.realskywars.utils.Text;
import josegamerpt.realskywars.utils.fastboard.FastBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RSWPlayer {

    private final List<Trail> trails = new ArrayList<>();
    private RoomTAB rt;
    private String anonName = "?";
    private Player p;
    private PlayerState state = PlayerState.LOBBY_OR_NOGAME;
    private String language = RealSkywars.getLanguageManager().getDefaultLanguage();
    private SWGameMode room;
    private SetupRoom setup;
    private Team team;
    private Cage cage;
    //statistics

    private int gamekills;

    private int totalkills;
    private int deaths;
    private int winsSolo;
    private int winsTEAMS;
    private int loses;
    private int gamesPlayed;

    private int rankedTotalkills;
    private int rankedDeaths;
    private int rankedWinsSolo;
    private int rankedWinsTEAMS;
    private int rankedLoses;
    private int rankedGamesPlayed;

    private Double coins = 0D;
    private Double balanceGame = 0D;
    private PlayerScoreboard playerscoreboard;
    private Material cageBlock = Material.GLASS;
    private ArrayList<String> bought = new ArrayList<>();
    private HashMap<Selections.Key, Selections.Value> selections = new HashMap<>();
    private Boolean bot = false;
    private Kit kit;
    private Particle bowParticle;
    private boolean winblockRandom = false;
    private Material winblockMaterial;
    private Boolean invincible = false;

    public RSWPlayer(Player jog, RSWPlayer.PlayerState estado, SWGameMode rom, int tk, int d, int solowin, int teamwin, Double coi, String lang,
                     ArrayList<String> bgh, int l, int gp, int rankedTotalkills, int rankedDeaths, int rankedWinsSolo, int rankedWinsTEAMS, int rankedLoses, int rankedGamesPlayed) {
        anonName = Text.anonName();

        this.p = jog;
        this.state = estado;
        this.room = rom;
        this.totalkills = tk;
        this.winsSolo = solowin;
        this.winsTEAMS = teamwin;
        this.deaths = d;
        this.coins = coi;
        this.language = lang;
        this.bought = bgh;
        this.loses = l;
        this.gamesPlayed = gp;
        this.playerscoreboard = new PlayerScoreboard(this);

        this.rankedTotalkills = rankedTotalkills;
        this.rankedDeaths = rankedDeaths;
        this.rankedWinsSolo = rankedWinsSolo;
        this.rankedWinsTEAMS = rankedWinsTEAMS;
        this.rankedLoses = rankedLoses;
        this.rankedGamesPlayed = rankedGamesPlayed;

        this.rt = new RoomTAB(this);
    }
    public RSWPlayer(boolean anonName) {
        if (anonName) {
            this.anonName = Text.anonName();
        }
        this.bot = true;
    }

    @Override
    public String toString() {
        return "RSWPlayer{" +
                "trails=" + trails +
                ", rt=" + rt +
                ", anonName='" + anonName + '\'' +
                ", p=" + p +
                ", state=" + state +
                ", language='" + language + '\'' +
                ", room=" + room +
                ", setup=" + setup +
                ", team=" + team +
                ", cage=" + cage +
                ", gamekills=" + gamekills +
                ", totalkills=" + totalkills +
                ", deaths=" + deaths +
                ", winsSolo=" + winsSolo +
                ", winsTEAMS=" + winsTEAMS +
                ", loses=" + loses +
                ", gamesPlayed=" + gamesPlayed +
                ", rankedTotalkills=" + rankedTotalkills +
                ", rankedDeaths=" + rankedDeaths +
                ", rankedWinsSolo=" + rankedWinsSolo +
                ", rankedWinsTEAMS=" + rankedWinsTEAMS +
                ", rankedLoses=" + rankedLoses +
                ", rankedGamesPlayed=" + rankedGamesPlayed +
                ", coins=" + coins +
                ", balanceGame=" + balanceGame +
                ", playerscoreboard=" + playerscoreboard +
                ", cageBlock=" + cageBlock +
                ", bought=" + bought +
                ", selections=" + selections +
                ", bot=" + bot +
                ", kit=" + kit +
                ", bowParticle=" + bowParticle +
                ", winblockRandom=" + winblockRandom +
                ", winblockMaterial=" + winblockMaterial +
                ", invincible=" + invincible +
                '}';
    }

    public void spawnAbovePlayer(Class c) {
        if (this.p != null) {
            Entity tnt = this.getWorld().spawn(this.getLocation().add(0, 3, 0), c);
            ((TNTPrimed) tnt).setFuseTicks(60);
        }
    }

    public void save() {
        for (RSWPlayer gp : RealSkywars.getPlayerManager().getPlayers()) {
            if (p != null && gp.getUUID().equals(p.getUniqueId())) {
                return;
            }
        }
        if (!this.bot) {
            RealSkywars.getPlayerManager().addPlayer(this);
        }
    }

    public boolean isInMatch() {
        return room != null;
    }

    public void addStatistic(RSWPlayer.Statistic t, int i, Boolean ranked) {
        switch (t) {
            case SOLO_WIN:
                if (ranked)
                {
                    this.rankedWinsSolo += i;
                } else {
                    this.winsSolo += i;
                }
                this.addStatistic(RSWPlayer.Statistic.GAMES_PLAYED, 1, ranked);
                this.balanceGame = (this.balanceGame + Config.file().getDouble("Config.Coins.Per-Win"));
                this.sendMessage("&e+ &6" + Config.file().getDouble("Config.Coins.Per-Win") + "&e coins");
                break;
            case TEAM_WIN:
                if (ranked)
                {
                    this.rankedWinsTEAMS += i;
                } else {
                    this.winsTEAMS += i;
                }
                this.addStatistic(RSWPlayer.Statistic.GAMES_PLAYED, 1, ranked);
                this.balanceGame = (this.balanceGame + Config.file().getDouble("Config.Coins.Per-Win"));
                this.sendMessage("&e+ &6" + Config.file().getDouble("Config.Coins.Per-Win") + "&e coins");
                break;
            case KILL:
                this.gamekills += i;
                this.balanceGame = (this.balanceGame + Config.file().getDouble("Config.Coins.Per-Kill"));
                this.sendMessage("&e+ &6" + Config.file().getDouble("Config.Coins.Per-Kill") + "&e coins");
                break;
            case LOSE:
                if (ranked)
                {
                    this.rankedLoses += i;
                } else {
                    this.loses += i;
                }
                break;
            case DEATH:
                if (ranked)
                {
                    this.rankedDeaths += i;
                } else {
                    this.deaths += i;
                }
                this.addStatistic(RSWPlayer.Statistic.LOSE, 1, ranked);
                this.addStatistic(RSWPlayer.Statistic.GAMES_PLAYED, 1, ranked);
                this.balanceGame = (this.balanceGame + Config.file().getDouble("Config.Coins.Per-Death"));
                this.sendMessage("&e- &6" + Config.file().getDouble("Config.Coins.Per-Death") + "&e coins");
                break;
            case GAMES_PLAYED:
                if (ranked)
                {
                    this.rankedGamesPlayed += i;
                } else {
                    this.gamesPlayed += i;
                }
                break;
        }
    }

    public void saveData() {
        this.totalkills += this.gamekills;
        this.coins += this.balanceGame;
        this.balanceGame = 0D;
        this.gamekills = 0;
        RealSkywars.getPlayerManager().savePlayer(this, PlayerData.ALL);
    }

    public double getGameBalance() {
        return (coins + balanceGame);
    }

    public void sendMessage(String string) {
        if (!this.bot) {
            p.sendMessage(Text.color(string));
        }
    }

    public void resetData() {
        Players.file().set(p.getUniqueId().toString(), null);
        Players.save();
        RealSkywars.getPlayerManager().removePlayer(this);
        p.kickPlayer(RealSkywars.getLanguageManager().getPrefix() + "§4Your data was resetted with success. \n §cPlease join the server again to complete the reset.");
    }

    public String getName() {
        if (p != null) {
            return p.getName();
        } else {
            return anonName;
        }
    }

    public void teleport(Location l) {
        if (p != null) {
            p.teleport(l);
        }
    }

    public void stopTrails() {
        trails.forEach(Trail::cancelTask);
    }

    public void addTrail(Trail t) {
        this.trails.add(t);
    }

    public void removeTrail(Trail t) {
        this.trails.remove(t);
    }

    public Location getLocation() {
        if (this.p != null) {
            return this.p.getLocation();
        }
        return null;
    }

    public void playSound(Sound s, int i, int i1) {
        if (this.p != null) {
            this.p.playSound(this.p.getLocation(), s, i, i1);
        }
    }

    public World getWorld() {
        if (this.p != null) {
            return this.p.getWorld();
        }
        return null;
    }

    public void executeWinBlock(int t) {
        if (t < 0) {
            return;
        }
        if (this.winblockRandom) {
            addTrail(new BlockWinTrail(this, t));
        } else {
            if (this.winblockMaterial != null) {
                addTrail(new BlockWinTrail(this, t, winblockMaterial));
            }
        }
    }

    public void delCage() {
        if (this.cage != null) this.cage.removePlayer(this);
    }

    public void setFlying(boolean b) {
        if (this.p != null) {
            if (b) {
                this.p.setAllowFlight(true);
                this.p.setFlying(true);
            } else {
                this.p.setAllowFlight(false);
                this.p.setFlying(false);
            }
        }
    }

    public UUID getUUID() {
        return p.getUniqueId();
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    public Player getPlayer() {
        return this.p;
    }

    public void setProperty(PlayerProperties pp, Object o) {
        switch (pp) {
            case KIT:
                this.kit = (Kit) o;
                break;
            case BOW_PARTICLES:
                this.bowParticle = (Particle) o;
                break;
            case CAGE_BLOCK:
                Material m = (Material) o;
                if (m != this.cageBlock) {
                    this.cageBlock = m;
                    if (isInMatch()) {
                        switch (this.getMatch().getGameMode()) {
                            case SOLO:
                                if (hasCage()) {
                                    this.cage.setCage();
                                }
                                break;
                            case TEAMS:
                                if (hasTeam()) {
                                    this.getTeam().getTeamCage().setCage();
                                }
                                break;
                        }
                    }
                }
                RealSkywars.getPlayerManager().savePlayer(this, PlayerData.PREFS);
                break;
            case WIN_BLOCKS:
                if (o.equals("RandomBlock")) {
                    this.winblockRandom = true;
                } else {
                    this.winblockRandom = false;
                    this.winblockMaterial = Material.getMaterial((String) o);
                }
                break;
            case STATE:
                this.state = (PlayerState) o;
                break;
            case LANGUAGE:
                this.language = (String) o;
                break;
        }
    }

    public Object getStatistics(PlayerStatistics pp, Boolean ranked) {
        switch (pp) {
            case LOSES:
                return ranked ? this.rankedLoses : this.loses;
            case DEATHS:
                return ranked ? this.rankedDeaths : this.deaths;
            case WINS_SOLO:
                return ranked ? this.rankedWinsSolo : this.winsSolo;
            case WINS_TEAMS:
                return ranked ? this.rankedWinsTEAMS : this.winsTEAMS;
            case TOTAL_KILLS:
                return ranked ? this.rankedTotalkills : this.totalkills;
            case GAMES_PLAYED:
                return ranked ? this.rankedGamesPlayed : this.gamesPlayed;
            case GAME_BALANCE:
                return this.balanceGame;
            case GAME_KILLS:
                return this.gamekills;
        }
        return null;
    }

    public Selections.Value getSelection(Selections.Key m) {
        return this.selections.get(m);
    }

    public void setSelection(Selections.Key s, Selections.Value ss) {
        this.selections.put(s, ss);
        RealSkywars.getPlayerManager().savePlayer(this, PlayerData.PREFS);
    }

    public RSWPlayer.PlayerState getState() {
        return this.state;
    }

    public SWGameMode getMatch() {
        return this.room;
    }

    public void setRoom(Object o) {
        this.room = (SWGameMode) o;
    }

    public SetupRoom getSetup() {
        return this.setup;
    }

    public void setSetup(SetupRoom o) {
        this.setup = o;
    }

    public HashMap<Selections.Key, Selections.Value> getSelections() {
        return this.selections;
    }

    public void setSelections(HashMap<Selections.Key, Selections.Value> ss) {
        this.selections = ss;
    }

    public List<String> getBoughtItems() {
        return this.bought != null ? this.bought : Collections.emptyList();
    }

    public PlayerScoreboard getScoreboard() {
        return this.playerscoreboard;
    }

    public Kit getKit() {
        return this.kit == null ? new Kit() : this.kit;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team o) {
        this.team = o;
    }

    public Object getProperty(PlayerProperties pp) {
        switch (pp) {
            case CAGE_BLOCK:
                return this.cageBlock;
            case BOW_PARTICLES:
                return this.bowParticle;
        }
        return null;
    }

    public double getCoins() {
        return this.coins;
    }

    public void setCoins(double v) {
        this.coins = v;
    }

    public void heal() {
        if (this.p != null) {
            this.p.setFireTicks(0);
            this.p.setHealth(20);
            this.p.setFoodLevel(20);

            this.p.getActivePotionEffects().forEach(potionEffect -> this.p.removePotionEffect(potionEffect.getType()));
        }
    }

    public Cage getCage() {
        return this.cage;
    }

    public void setCage(Cage c) {
        this.cage = c;
    }

    public boolean isInvencible() {
        return this.invincible;
    }

    public void setInvincible(boolean b) {
        this.invincible = b;
    }

    public void leave() {
        if (this.room != null) {
            this.room.removePlayer(this);
        }

        this.stopTrails();

        this.playerscoreboard.stop();
        this.saveData();
        RealSkywars.getPlayerManager().removePlayer(this);
    }

    public void sendTitle(String s, String s1, int i, int i1, int i2) {
        if (this.p != null) this.p.sendTitle(s, s1, i, i1, i2);
    }

    public boolean hasKit() {
        return this.kit != null;
    }

    public boolean hasCage() {
        return this.cage != null;
    }

    public PlayerInventory getInventory() {
        return this.p.getInventory();
    }

    public boolean hasTeam() {
        return this.team != null;
    }

    public CharSequence getDisplayName() {
        return this.bot ? this.anonName : this.p.getDisplayName();
    }

    public void sendCenterMessage(String r) {
        sendMessage(Text.centerMessage(r));
    }

    public boolean isBot() {
        return this.bot;
    }

    public void hidePlayer(Plugin plugin, Player pl) {
        if (!this.bot && pl != null && !RealSkywars.getGameManager().endingGames) this.getPlayer().hidePlayer(plugin, pl);
    }

    public void showPlayer(Plugin plugin, Player pl) {
        if (!this.bot && pl != null && !RealSkywars.getGameManager().endingGames) this.getPlayer().showPlayer(plugin, pl);
    }

    public RoomTAB getTab() {
        return this.rt;
    }

    public void buyItem(String s) {
        this.bought.add(ChatColor.stripColor(s));
        RealSkywars.getPlayerManager().savePlayer(this, PlayerData.PREFS);
        RealSkywars.getPlayerManager().savePlayer(this, PlayerData.BOUGHT);
    }

    public void sendActionbar(String s) {
        if (this.p != null) this.p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Text.color(s)));
    }

    public enum PlayerData {ALL, COINS, STATS, NAME, LANG, BOUGHT, PREFS }

    public enum Statistic {KILL, SOLO_WIN, TEAM_WIN, LOSE, DEATH, GAMES_PLAYED }

    //ENUMs

    public enum PlayerState {
        LOBBY_OR_NOGAME, CAGE, PLAYING, SPECTATOR, EXTERNAL_SPECTATOR
    }

    public enum PlayerProperties {KIT, BOW_PARTICLES, CAGE_BLOCK, STATE, LANGUAGE, WIN_BLOCKS}

    public enum PlayerStatistics {WINS_SOLO, WINS_TEAMS, TOTAL_KILLS, DEATHS, LOSES, GAMES_PLAYED, GAME_BALANCE, GAME_KILLS}

    //TAB per player

    public class RoomTAB {

        private final RSWPlayer player;
        private ArrayList<Player> show = new ArrayList<>();

        public RoomTAB(RSWPlayer player) {
            this.player = player;
            clear();
        }

        public void add(Player p) {
            if (p.getUniqueId() != this.player.getUUID() && !show.contains(p)) {
                show.add(p);
            }
        }

        public void add(List<Player> p) {
            show.addAll(p);
        }


        public void remove(Player p) {
            show.remove(p);
        }

        public void reset() {
            this.show.addAll(Bukkit.getOnlinePlayers());
        }

        public void clear() {
            this.show.clear();
        }

        public void setHeaderFooter(String h, String f) {
            if (!this.player.isBot()) {
                this.player.getPlayer().setPlayerListHeaderFooter(Text.color(h), Text.color(f));
            }
        }


        public void updateRoomTAB() {
            if (!this.player.isBot()) {
                Bukkit.getOnlinePlayers().forEach(pl -> this.player.hidePlayer(RealSkywars.getPlugin(), pl));
                this.show.forEach(rswPlayer -> this.player.showPlayer(RealSkywars.getPlugin(), rswPlayer));

                if (this.player.isInMatch()) {
                    this.setHeaderFooter("\n" + RealSkywars.getLanguageManager().getPrefix() + "\n&fMapa: &b" + this.player.getMatch().getName() + "\n",
                            "\n&fJogadores: &b" + this.player.getMatch().getPlayersCount() + "\n");
                } else {
                    this.setHeaderFooter("", "");
                }
            }
        }
    }


    //Player Scoreboard

    public class PlayerScoreboard {

        private FastBoard fb;
        private RSWPlayer linked;
        private BukkitTask task;

        public PlayerScoreboard(RSWPlayer r) {
            this.linked = r;
            this.fb = new FastBoard(r.getPlayer());
            if (RealSkywars.getGameManager().getLobbyLocation() != null)
                run();
        }

        protected String variables(String s, RSWPlayer gp) {
            if (gp.isInMatch()) {
                return s.replace("%space%", Text.makeSpace())
                        .replace("%players%", gp.getMatch().getPlayersCount() + "")
                        .replace("%nextevent%", nextEvent(gp.getMatch()))
                        .replace("%spectators%", gp.getMatch().getSpectatorsCount() + "")
                        .replace("%kills%", gp.getStatistics(RSWPlayer.PlayerStatistics.GAME_KILLS, gp.getMatch().isRanked()) + "")
                        .replace("%map%", gp.getMatch().getName())
                        .replace("%runtime%", Text.formatSeconds(gp.getMatch().getTimePassed()) + "")
                        .replace("%state%", RealSkywars.getGameManager().getStateString(gp, gp.getMatch().getState()))
                        .replace("%mode%", gp.getMatch().getGameMode().name())
                        .replace("%solowins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_SOLO, gp.getMatch().isRanked()) + "")
                        .replace("%teamwins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_TEAMS, gp.getMatch().isRanked()) + "")
                        .replace("%loses%", gp.getStatistics(RSWPlayer.PlayerStatistics.LOSES, gp.getMatch().isRanked()) + "")
                        .replace("%gamesplayed%", gp.getStatistics(RSWPlayer.PlayerStatistics.GAMES_PLAYED, gp.getMatch().isRanked()) + "");
            } else {
                return s.replace("%space%", Text.makeSpace())
                        .replace("%coins%", gp.getCoins() + "")
                        .replace("%playing%", "" + RealSkywars.getPlayerManager().countPlayingPlayers())
                        .replace("%kills%", gp.getStatistics(RSWPlayer.PlayerStatistics.TOTAL_KILLS, false) + "")
                        .replace("%deaths%", gp.getStatistics(RSWPlayer.PlayerStatistics.DEATHS, false) + "")
                        .replace("%solowins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_SOLO, false) + "")
                        .replace("%teamwins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_TEAMS, false) + "")
                        .replace("%loses%", gp.getStatistics(RSWPlayer.PlayerStatistics.LOSES, false) + "")
                        .replace("%gamesplayed%", gp.getStatistics(RSWPlayer.PlayerStatistics.GAMES_PLAYED, false) + "")
                        .replace("%playing%", "" + RealSkywars.getPlayerManager().countPlayingPlayers())
                        .replace("%rankedkills%", gp.getStatistics(RSWPlayer.PlayerStatistics.TOTAL_KILLS, true) + "")
                        .replace("%rankeddeaths%", gp.getStatistics(RSWPlayer.PlayerStatistics.DEATHS, true) + "")
                        .replace("%rankedsolowins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_SOLO, true) + "")
                        .replace("%rankedteamwins%", gp.getStatistics(RSWPlayer.PlayerStatistics.WINS_TEAMS, true) + "")
                        .replace("%rankedloses%", gp.getStatistics(RSWPlayer.PlayerStatistics.LOSES, true) + "")
                        .replace("%rankedgamesplayed%", gp.getStatistics(RSWPlayer.PlayerStatistics.GAMES_PLAYED, true) + "");
            }
        }

        private String nextEvent(SWGameMode match) {
            if (match.getEvents().size() > 0) {
                return match.getEvents().get(0).getName();
            }
            return "-";
        }

        public void stop() {
            if (this.task != null) {
                this.task.cancel();
            }
            this.fb.delete();
        }

        public void run() {
            this.task = new BukkitRunnable() {
                public void run() {
                    ArrayList<String> lista;
                    String tit;
                    if (linked.getState() != null) {
                        switch (linked.getState()) {
                            case LOBBY_OR_NOGAME:
                                if (!RealSkywars.getGameManager().scoreboardInLobby()) {
                                    return;
                                }
                                if (RealSkywars.getGameManager().getLobbyLocation().getWorld() != linked.getWorld()) {
                                    return;
                                }
                                lista = RealSkywars.getLanguageManager().getList(linked, LanguageManager.TL.SCOREBOARD_LOBBY_LINES);
                                tit = RealSkywars.getLanguageManager().getString(linked, LanguageManager.TS.SCOREBOARD_LOBBY_TITLE, false);
                                break;
                            case CAGE:
                                lista = RealSkywars.getLanguageManager().getList(linked, LanguageManager.TL.SCOREBOARD_CAGE_LINES);
                                tit = RealSkywars.getLanguageManager().getString(linked, LanguageManager.TS.SCOREBOARD_CAGE_TITLE, false).replace("%map%", linked.getMatch().getName()).replace("%mode%", linked.getMatch().getGameMode().name());
                                break;
                            case SPECTATOR:
                            case EXTERNAL_SPECTATOR:
                                lista = RealSkywars.getLanguageManager().getList(linked, LanguageManager.TL.SCOREBOARD_SPECTATOR_LINES);
                                tit = RealSkywars.getLanguageManager().getString(linked, LanguageManager.TS.SCOREBOARD_SPECTATOR_TITLE, false).replace("%map%", linked.getMatch().getName()).replace("%mode%", linked.getMatch().getGameMode().name());
                                break;
                            case PLAYING:
                                lista = RealSkywars.getLanguageManager().getList(linked, LanguageManager.TL.SCOREBOARD_PLAYING_LINES);
                                tit = RealSkywars.getLanguageManager().getString(linked, LanguageManager.TS.SCOREBOARD_PLAYING_TITLE, false).replace("%map%", linked.getMatch().getName()).replace("%mode%", linked.getMatch().getGameMode().name());
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value SCOREBOARD!!! : " + linked.getState());
                        }

                        ArrayList<String> send = new ArrayList<>();
                        for (String s : lista) {
                            send.add(variables(s, linked));
                        }
                        displayScoreboard(tit, send);
                    }
                }
            }.runTaskTimer(RealSkywars.getPlugin(), 0L, 20);
        }

        private void displayScoreboard(String title, ArrayList<String> elements) {
            this.fb.updateTitle(title);
            this.fb.updateLines(elements);
        }
    }
}
