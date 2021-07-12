package ua.darkphantom1337.magix.boss;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class BoosterCMD implements CommandExecutor {

    private Main plugin;

    public BoosterCMD(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 0 && s.contains("thx")){
                if (plugin.global_booster == 1.0){
                    p.sendMessage("§cНа данный момент нет активного бустера.");
                    return false;
                }
                try {
                    if (plugin.thxplayers.contains(p.getUniqueId())){
                        p.sendMessage("§cВы уже благодарили данного игрока за этот бустер.");
                        return false;
                    }
                    plugin.getEconomy().depositPlayer(plugin.booster_player_name, 5.0);
                    plugin.thxplayers.add(p.getUniqueId());
                    p.sendMessage("§aВы отблагодарили игрока " + plugin.booster_player_name + " :-)");
                    return true;
                } catch (Exception e){
                    p.sendMessage("§cК сожалению не удалось отблагодарить игрока " + plugin.booster_player_name + " :-(");
                }
                return false;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("check")){
                String playerName = args[1];
                Player pl;
                try {
                    pl = Bukkit.getPlayer(playerName);
                    p.sendMessage("§a[Check-Booster] -> Global: " + plugin.global_booster
                    + "\n§aPerm for " + playerName + ": " + MagixBoss.getPermGlobalBooster(pl)
                    + "\n§aTotal for " + playerName + ": " + plugin.global_booster*MagixBoss.getPermGlobalBooster(pl));
                } catch (Exception e){
                    p.sendMessage("§a[Check-Booster-Error] -> Global: " + plugin.global_booster);
                }
                return true;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("setGlobalTo")){
                Double bb;
                try {
                    bb = Double.parseDouble(args[1]);
                    plugin.global_booster = bb;
                    p.sendMessage("§a[Check-Booster] -> Global: " + plugin.global_booster);
                } catch (Exception e){
                    p.sendMessage("§a[Check-Booster-Error] -> Global: " + plugin.global_booster);
                }
                return true;
            }
            if (args.length == 2) {
                String booster = args[0];
                Integer hours = Integer.parseInt(args[1]);
                if (plugin.enabled_boosters.contains(booster)) {
                    if (plugin.global_booster == 1.0) {
                        p.sendMessage("§aВы успешно купили бустер. В течении пары секунд он станет активным.");
                        plugin.thxplayers = new ArrayList<>();
                        plugin.global_booster = Double.parseDouble(booster.replace("-", "."));
                        plugin.booster_player_name = p.getName();
                        plugin.booster_timing = (hours * 60);
                        new BukkitRunnable() {
                            /**
                             * When an object implementing interface <code>Runnable</code> is used
                             * to create a thread, starting the thread causes the object's
                             * <code>run</code> method to be called in that separately executing
                             * thread.
                             * <p>
                             * The general contract of the method <code>run</code> is that it may
                             * take any action whatsoever.
                             *
                             * @see Thread#run()
                             */
                            @Override
                            public void run() {
                                plugin.booster_player_name = "";
                                plugin.global_booster = 1.0;
                            }
                        }.runTaskLater(plugin, (20 * 60) * hours);
                        plugin.booster_bar = plugin.getBossBar();
                        plugin.booster_bar.setProgress(1.0);
                        plugin.booster_bar.setVisible(true);
                        for (Player pl : Bukkit.getOnlinePlayers()) {
                            try {
                                if (plugin.cfg.getIgnoredBoosterWorldName().contains(pl.getWorld().getName()))
                                    continue;
                                plugin.booster_bar.addPlayer(pl);
                            } catch (Exception e) {
                            }
                        }
                        plugin.startBossBarUpdater();
                    } else {
                        p.sendMessage("§cНа данный момент в мирах уже есть активный бустер, подождите пока он закончиться...");
                    }
                }
                return true;
            }
        }
        return false;
    }
}
