package ua.darkphantom1337.magix.boss;

import org.bukkit.Statistic;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import ua.darkphantom1337.magixrub.PlayerWalletFile;
import ua.darkphantom1337.magixwands.MagixWandAPI;

import java.util.UUID;

public class Listeners implements Listener {

    private Main plugin;

    public Listeners(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player || (e.getDamager() instanceof Projectile ) ) {
            Player damager = null;
            Boolean isProjectile = e.getDamager() instanceof Projectile;
            try {
                if (e.getDamager() instanceof Player)
                    damager = (Player) e.getDamager();
                else damager = (Player) ((Projectile) e.getDamager()).getShooter();
            } catch (Exception ee){

            }
            if (e.getEntity() == null || damager == null)
                return;
            LivingEntity victim = (LivingEntity) e.getEntity();
            UUID victimUUID = victim.getUniqueId();
            Boolean isKill = false, isWand = false;
            Double fdamge = e.getFinalDamage();
            if (isProjectile && MagixWandAPI.plugin.projectile_damage.containsKey(e.getDamager().getUniqueId())) {
                fdamge = MagixWandAPI.plugin.projectile_damage.get(e.getDamager().getUniqueId());
                isWand = true;
            }
            if (e.getFinalDamage() >= victim.getHealth() || fdamge >= victim.getHealth()) {// MobKill{
                isKill = true;
                fdamge = victim.getHealth();
                if (isWand && victim instanceof Player) {
                    damager.setStatistic(Statistic.PLAYER_KILLS, damager.getStatistic(Statistic.PLAYER_KILLS)+1);
                }
                }
            if (!plugin.isDamagedMob(victimUUID))
                plugin.addDamagedMobs(victimUUID, new MagixBoss(victim, plugin));
            MagixBoss damaged_mob = plugin.getDamagedMobs().get(victimUUID);
            if (plugin.getEnabledMobs().contains(victim.getType())) {
                damaged_mob.damage(damager, fdamge, isKill);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        plugin.in_cuboids_players.remove(e.getEntity().getUniqueId());
        plugin.in_cuboids_players_time.remove(e.getEntity().getUniqueId());
    }

}
