/*
 * @Author DarkPhantom1337
 * @Version 1.0.0
 */
package ua.darkphantom1337.magix.boss;

import com.destroystokyo.paper.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Main extends JavaPlugin {

    public ConfigFile cfg;
    private static Economy econ = null;

    public List<EntityType> enabled_mobs;
    public List<String> enabled_boosters;

    public HashMap<UUID, MagixBoss> damaged_mobs;
    public HashMap<String, List<Cuboid>> all_regions = new HashMap<>();
    public HashMap<UUID, Cuboid> in_cuboids_players = new HashMap<>();
    public HashMap<UUID, Integer> in_cuboids_players_time = new HashMap<>();

    public Double global_booster = 1.0;
    public String booster_player_name = "";
    public Integer booster_timing = 0;
    public BossBar booster_bar;

    public List<UUID> thxplayers = new ArrayList<>();

    public void onEnable() {
        try {
            cfg = new ConfigFile(this, "config.yml");
            getCommand("booster").setExecutor(new BoosterCMD(this));
            Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
            if (!setupEconomy()) {
                getLogger().fine(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                new MagixBossPlaceholder(this).register();
            else
                getLogger().info("Placeholder API not found!");
            setEnabledMobs(cfg.getEnabledMobs());
            enabled_boosters = cfg.getEnabledBoosters();
            setDamagedMobs(new HashMap<>());
            for (String s : cfg.getManaRegionsName()) {
                try {
                    String[] spl = s.split(";");
                    Location one = new Location(Bukkit.getWorld(spl[0]), Integer.parseInt(spl[1])
                            , Integer.parseInt(spl[2]), Integer.parseInt(spl[3]));
                    Location two = new Location(Bukkit.getWorld(spl[0]), Integer.parseInt(spl[4])
                            , Integer.parseInt(spl[5]), Integer.parseInt(spl[6]));
                    if (!all_regions.containsKey(spl[0])) {
                        all_regions.put(spl[0], Arrays.asList(new Cuboid(one, two)));
                    } else {
                        List<Cuboid> world_cuboids = all_regions.get(spl[0]);
                        world_cuboids.add(new Cuboid(one, two));
                        all_regions.put(spl[0], world_cuboids);
                    }
                } catch (Exception e) {
                    getLogger().fine("Error in add region mana -> " + s + "\nError -> " + e.getLocalizedMessage());
                }
            }
            startRegionJoinUpdater();
            startInRegionPlayersUpdater();
            Bukkit.getConsoleSender()
                    .sendMessage("§a[§eMagixBoss§a] §f-> §aPlugin successfully enabled! // by DarkPhantom1337, 2021");
        } catch (Exception e) {
            Bukkit.getConsoleSender()
                    .sendMessage("§c[§eMagixBoss§c] §f-> §cError in enabling plugin! Plugin disabled!\nError:"
                            + e.getLocalizedMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


    public Economy getEconomy() {
        return econ;
    }

    public void startBossBarUpdater() {
        new BukkitRunnable() {

            public void run() {
                try {
                    if (global_booster == 1.0) {
                        booster_bar.removeAll();
                        booster_bar.setVisible(false);
                        this.cancel();
                        return;
                    }
                    double shag = (double) (1.0 / booster_timing);
                    booster_bar.setProgress(booster_bar.getProgress() - shag);
                } catch (Exception e) {

                }
            }
        }.runTaskTimer(this, 20, 20);
    }

    public void startRegionJoinUpdater() {
        new BukkitRunnable() {

            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    try {
                        if (all_regions.containsKey(p.getWorld().getName())) {
                            Location ploc = p.getLocation();
                            for (Cuboid cuboid : all_regions.get(p.getWorld().getName())) {
                                if (in_cuboids_players.containsKey(p.getUniqueId()))
                                    continue;
                                if (cuboid.contains(ploc)) {
                                    in_cuboids_players_time.put(p.getUniqueId(), 0);
                                    in_cuboids_players.put(p.getUniqueId(), cuboid);
                                }
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }.runTaskTimer(this, 20, 20 * 10);
    }

    public void startInRegionPlayersUpdater() {
        new BukkitRunnable() {

            public void run() {
                HashMap<UUID, Cuboid> ii = (HashMap<UUID, Cuboid>) in_cuboids_players.clone();
                for (UUID id : ii.keySet()) {
                    try {
                        if (in_cuboids_players.get(id).contains(Bukkit.getPlayer(id).getLocation())) {
                            Integer time = in_cuboids_players_time.get(id);
                           try {
                               Bukkit.getPlayer(id).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§7[§5!§7] §fОсталось §e" + time + " §fдо следующего пополнения маны!"));
                           } catch (Exception e ){

                           }if (time >= 60) {
                                time = 0;
                                for (String cmd : cfg.getEveryMinuteCommands())
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%name%", Bukkit.getPlayer(id).getName()));
                                Bukkit.getPlayer(id).sendTitle(new Title("§b+ 1☯"));
                            } else time++;

                            in_cuboids_players_time.put(id, time);
                        } else {
                            in_cuboids_players.remove(id);
                            in_cuboids_players_time.remove(id);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }.runTaskTimer(this, 20, 20);
    }

    public List<EntityType> getEnabledMobs() {
        return enabled_mobs;
    }

    public void setEnabledMobs(List<EntityType> enabled_mobs) {
        this.enabled_mobs = enabled_mobs;
    }


    public HashMap<UUID, MagixBoss> getDamagedMobs() {
        return damaged_mobs;
    }

    public void setDamagedMobs(HashMap<UUID, MagixBoss> damaged_mobs) {
        this.damaged_mobs = damaged_mobs;
    }

    public void addDamagedMobs(UUID mobUUID, MagixBoss mobdata) {
        getDamagedMobs().put(mobUUID, mobdata);
    }

    public Boolean isDamagedMob(UUID mobUUID) {
        return getDamagedMobs().containsKey(mobUUID);
    }

    public void remDamagedMobs(UUID mobUUID) {
        getDamagedMobs().remove(mobUUID);
    }

    public BossBar getBossBar() {
        BossBar bar = Bukkit.createBossBar(cfg.getS("BossBar.Text").replace("%value%", "" + global_booster).replace("%name%", booster_player_name), BarColor.valueOf(cfg.getS("BossBar.Color").toUpperCase()), BarStyle.SOLID, BarFlag.CREATE_FOG,
                BarFlag.DARKEN_SKY, BarFlag.PLAY_BOSS_MUSIC);
        return bar;
    }

}