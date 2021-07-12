/*
 * @Author DarkPhantom1337
 * @Version 1.0.0
 */
package ua.darkphantom1337.magix.boss;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigFile {

    private FileConfiguration filecfg;
    private Main plugin;
    private File file;
    private String filename;
    private String pluginname;

    public ConfigFile(Main plugin, String filename) {
        this.plugin = plugin;
        this.filename = filename;
        this.pluginname = plugin.getName();
        setupCfgFile();
        if (getCfgFile().isSet(pluginname))
            saveCfgFile();
        else
            firstFill();
    }

    private void setupCfgFile() {
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        file = new File(plugin.getDataFolder(), filename);
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException localIOException) {
                System.out.println("Error in creating file " + filename + "!");
            }
        filecfg = YamlConfiguration.loadConfiguration(file);
    }

    private FileConfiguration getCfgFile() {
        return filecfg;
    }

    public void saveCfgFile() {
        try {
            filecfg.save(file);
        } catch (IOException localIOException) {
            System.out.println("Error in saving file " + filename + "!");
        }
    }

    public void reloadCfgFile() {
        filecfg = YamlConfiguration.loadConfiguration(file);
    }

    private void firstFill() {
        getCfgFile().set(pluginname, " File: " + filename + " || Author: DarkPhantom1337");
        getCfgFile().set("EnabledMobs", Arrays.asList("WITCH", "ZOMBIE"));
        getCfgFile().set("witch.Reward", 3000);
        getCfgFile().set("witch.Announce", "§7[§5!§7] §fВедьма была повержена, последнюю долю урона нанес игрок: %last_damager%");
        getCfgFile().set("witch.IgnoreWorlds", Arrays.asList("spawn"));
        getCfgFile().set("zombie.Reward", 3000);
        getCfgFile().set("zombie.Announce", "§7[§5!§7] §fВедьма была повержена, последнюю долю урона нанес игрок: %last_damager%");
        getCfgFile().set("zombie.IgnoreWorlds", Arrays.asList("spawn"));
        getCfgFile().set("Messages.ForKill", "§7[§5!§7] §fЗа нанесённый урон по боссу вы получили &e%money%");
        getCfgFile().set("Boosters.Enabled", Arrays.asList("1-5", "1-9", "2"));
        getCfgFile().set("Boosters.1-5.Price", 300);
        getCfgFile().set("Boosters.1-9.Price", 500);
        getCfgFile().set("Boosters.2.Price", 600);
        getCfgFile().set("Boosters.IgnoreWorlds", Arrays.asList("spawn"));
        getCfgFile().set("BossBar.Text", "§aБустер денег %value% от %name%. /thx ");
        getCfgFile().set("BossBar.Color", "purple");
        /**
         * мир;х1;у1;z1;x2;y2;z2
         */
        getCfgFile().set("ManaRegions", Arrays.asList("location1;11;70;100;20;80;120"));
        getCfgFile().set("EveryMinuteCommands", Arrays.asList("magixexp give %name% 1"));

        saveCfgFile();
    }



    public List<String> getManaRegionsName() {
        return getCfgFile().getStringList("ManaRegions");
    }


    public List<String> getEveryMinuteCommands() {
        return getCfgFile().getStringList("EveryMinuteCommands");
    }

    public List<EntityType> getEnabledMobs() {
        List<EntityType> ent = new ArrayList<>();
        for (String s : getCfgFile().getStringList("EnabledMobs"))
            try {
                ent.add(EntityType.valueOf(s.toUpperCase()));
            } catch (Exception e) {
                plugin.getLogger().fine("Error in getting enabled mobs...\nMob: " + s + " Please check... Mob disabled");
            }
        return ent;
    }


    public void setS(String path, String value) {
        getCfgFile().set(path, value);
        saveCfgFile();
    }

    public Integer getInt(String path) {
        return getCfgFile().getInt(path);
    }

    public String getS(String path) {
        return getCfgFile().getString(path).replaceAll("&", "§");
    }


    public List<String> getIgnoredWorldsName(String entityID) {
        return getCfgFile().getStringList(entityID + ".IgnoreWorlds");
    }

    public List<String> getIgnoredBoosterWorldName() {
        return getCfgFile().getStringList("Boosters.IgnoreWorlds");
    }

    public List<String> getEnabledBoosters() {
        return getCfgFile().getStringList("Boosters.Enabled");
    }

    public Boolean getB(String path) {
        return getCfgFile().getBoolean(path);
    }

}
