package subside.plugins.koth.hooks.plugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import lombok.Getter;
import subside.plugins.koth.ConfigHandler;
import subside.plugins.koth.ConfigHandler.Hooks.Featherboard;
import subside.plugins.koth.adapter.Koth;
import subside.plugins.koth.events.KothEndEvent;
import subside.plugins.koth.events.KothStartEvent;
import subside.plugins.koth.hooks.AbstractHook;

public class FeatherboardHook extends AbstractHook implements Listener {
    private @Getter boolean enabled = false;
    private Koth koth;
    
    
    private @Getter int range = 20;
    private @Getter int rangeMargin = 5;
    private @Getter String board;
    private List<OfflinePlayer> inRange;
    
    public FeatherboardHook(){
        inRange = new ArrayList<>();
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("FeatherBoard")){
            Featherboard fbHook = ConfigHandler.getCfgHandler().getHooks().getFeatherboard();
            if(fbHook.isEnabled()){
                enabled = true;
                range = fbHook.getRange();
                rangeMargin = fbHook.getRangeMargin();
                board = fbHook.getBoard();
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onKothStart(KothStartEvent event){
        koth = event.getKoth();
    }

    @EventHandler(ignoreCancelled = true)
    public void onKothEnd(KothEndEvent event){
        if(!isEnabled() || koth == null) return;
        
        for(OfflinePlayer player : inRange){
            if(player.isOnline())
                resetBoard(player.getPlayer(), board);
        }
        inRange.clear();
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        if(range < 0){
            inRange.add(event.getPlayer());
            setBoard(event.getPlayer(), board);
        }
    }
    
    
    @Override
    public void tick(){
        if(!isEnabled() || koth == null) return;
        
        for(Player player: Bukkit.getOnlinePlayers()){
            if(!inRange.contains(player)){
                if(koth.getMiddle().distance(player.getLocation()) <= range-rangeMargin){
                    inRange.add(player);
                    setBoard(player, board);
                }
            } else {
                if(koth.getMiddle().distance(player.getLocation()) >= range+rangeMargin){
                    inRange.remove(player);
                    resetBoard(player, board);
                }
            }
        }
    }

    
    public void setBoard(Player player, String board){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fb show "+player.getName()+" "+board);
    }
    
    public void resetBoard(Player player, String board){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fb hide "+player.getName()+" "+board);
    }
    
}