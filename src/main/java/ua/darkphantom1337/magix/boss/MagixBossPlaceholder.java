package ua.darkphantom1337.magix.boss;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class MagixBossPlaceholder extends PlaceholderExpansion {

    private Main plugin;

    public MagixBossPlaceholder(Main plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return "magixboss";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if(identifier.equals("global_booster"))
            return plugin.global_booster.toString();
        if(identifier.equals("perm_booster"))
            return MagixBoss.getPermGlobalBooster(player).toString();
        if(identifier.equals("total_booster"))
            return "" + (MagixBoss.getPermGlobalBooster(player) * plugin.global_booster);
        return null;
    }
}

