package ua.darkphantom1337.magix.boss;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MagixBoss {

    private Main plugin;
    private String entityID;

    private Player last_damager;
    private HashMap<UUID, Double> all_damagers;
    private Double maxHealth;
    private UUID entUUID;
    private Entity entity;

    public MagixBoss(LivingEntity entity, Main plugin) {
        this.plugin = plugin;
        this.entity = entity;
        if (plugin.getEnabledMobs().contains(entity.getType())) {
            entityID = entity.getType().toString().toLowerCase();
            all_damagers = new HashMap<UUID, Double>();
            maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            entUUID = entity.getUniqueId();
        }
    }

    public Integer getReward() {
        return plugin.cfg.getInt(entityID + ".Reward");
    }

    public String getAnnounceMessage() {
        return plugin.cfg.getS(entityID + ".Announce");
    }

    public List<String> getIgnoreWorldName() {
        return plugin.cfg.getIgnoredWorldsName(entityID);
    }

    public Player getLastDamager() {
        return last_damager;
    }

    public void setLastDamager(Player last_damager) {
        this.last_damager = last_damager;
    }

    public void damage(Player p, Double finalDamage, Boolean isKill) {
        if (p != null) {
            if (!plugin.getEnabledMobs().contains(entity.getType())) {
                return;
            }
            if (getIgnoreWorldName().contains(p.getWorld().getName()))
                return;
            UUID uuid = p.getUniqueId();
            if (getAllDamagers().containsKey(uuid))
                getAllDamagers().put(uuid, getAllDamagers().get(uuid) + finalDamage);
            else
                getAllDamagers().put(uuid, finalDamage);
            setLastDamager(p);
            if (isKill) {
                if (!getAnnounceMessage().equalsIgnoreCase("off"))
                    for (Player player : Bukkit.getOnlinePlayers())
                        if (!plugin.cfg.getIgnoredWorldsName(entityID.toLowerCase()).contains(player.getWorld().getName()))
                            player.sendMessage(getAnnounceMessage().replace("%last_damager%", getLastDamager().getName()));
                /**
                 * Calculate rewards .... 1 % damage = 1 % reward
                 */
                maxHealth = 0.0;
                for (UUID damager : getAllDamagers().keySet())
                    maxHealth += getAllDamagers().get(damager);
                for (UUID damager : getAllDamagers().keySet()) {
                    Double percentOfDamage = (100 * getAllDamagers().get(damager)) / maxHealth;
                    if (percentOfDamage > 100)
                        percentOfDamage = 100.0;
                    Double percentOfReward = percentOfDamage;
                    Double reward = (((getReward() * (percentOfReward / 100))
                            * getPermGlobalBooster(Bukkit.getPlayer(damager)))) * plugin.global_booster;
                    plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(damager), reward);
                    Bukkit.getPlayer(damager).sendMessage(plugin.cfg.getS("Messages.ForKill").replace("%money%", "" + reward.intValue()));
                }
                plugin.remDamagedMobs(entUUID);
            }
        }
    }

    public static Double getPermGlobalBooster(Player p) {
        if (p.hasPermission("boost.don5"))
            return 2.0;
        if (p.hasPermission("boost.don4"))
            return 1.8;
        if (p.hasPermission("boost.don3"))
            return 1.6;
        if (p.hasPermission("boost.don2"))
            return 1.4;
        if (p.hasPermission("boost.don1"))
            return 1.2;
        return 1.0;
    }

    public void setAllDamagers(HashMap<UUID, Double> all_damagers) {
        this.all_damagers = all_damagers;
    }

    public HashMap<UUID, Double> getAllDamagers() {
        if (all_damagers == null)
            this.all_damagers = new HashMap<UUID, Double>();
        return this.all_damagers;
    }

    public void addDamager(UUID damagerUUID) {
        getAllDamagers().put(damagerUUID, 0.0);
    }

}
