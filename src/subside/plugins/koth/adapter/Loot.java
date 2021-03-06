package subside.plugins.koth.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import lombok.Getter;
import lombok.Setter;
import subside.plugins.koth.ConfigHandler;
import subside.plugins.koth.Lang;
import subside.plugins.koth.adapter.captypes.Capper;
import subside.plugins.koth.utils.MessageBuilder;
import subside.plugins.koth.utils.Utils;

/**
 * @author Thomas "SubSide" van den Bulk
 *
 */
public class Loot {

    private @Getter Inventory inventory;
    private @Getter @Setter String name;
    private @Getter List<String> commands;
    
    public Loot(String name){
        inventory = Bukkit.createInventory(null, 54, getTitle(name));
        this.name = name;
        this.commands = new ArrayList<>();
    }
    
    public Loot(String name, List<String> commands){
        this(name);
        this.commands = commands;
    }

    /** Get the title by the loot name
     * 
     * @param name      The name of the loot
     * @return          The marked-up title
     */
    public static String getTitle(String name){
        String title = new MessageBuilder(Lang.COMMAND_LOOT_CHEST_TITLE).loot(name).build()[0];
        if (title.length() > 32) title = title.substring(0, 32);
        return title;
    }
    
    public void triggerCommands(Koth koth, Capper capper){
        if(!ConfigHandler.getCfgHandler().getLoot().isCmdEnabled()){
            return;
        }
        
        for(String command : commands){
            List<Player> players = capper.getAvailablePlayers(koth);
            if(command.contains("%player%")){
                for(Player player : players){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", player.getName()));
                }
            } else if(command.contains("%faction%")){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%faction%", capper.getName()));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
    

    @Deprecated
    public static Loot load(JSONObject obj) {
        Loot loot = new Loot((String)obj.get("name"));
        
        JSONObject lootItems = (JSONObject)obj.get("items");
        for(Object key : lootItems.keySet()){
            try {
                loot.inventory.setItem(Integer.parseInt((String)key), Utils.itemFrom64((String)lootItems.get(key)));
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        
        if(obj.containsKey("commands")){
            JSONArray commands = (JSONArray)obj.get("commands");
            Iterator<?> it = commands.iterator();
            while(it.hasNext()){
                try {
                    loot.commands.add((String)it.next());
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        
        
        return loot;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public JSONObject save(){
        JSONObject obj = new JSONObject();
        obj.put("name", this.name); // name
        
        if(inventory.getSize() > 0){
            JSONObject lootItems = new JSONObject();
            for (int x = 0; x < 54; x++) {
                ItemStack item = inventory.getItem(x);
                if (item != null) {
                    lootItems.put(x, Utils.itemTo64(item));
                }
            }
            obj.put("items", lootItems); // items
        }
        
        if(commands.size() > 0){
            JSONArray commandz = new JSONArray();
            for(String command : commands){
                commandz.add(command);
            }
            
            obj.put("commands", commandz); // commands
        }
        
        return obj;
    }
}
