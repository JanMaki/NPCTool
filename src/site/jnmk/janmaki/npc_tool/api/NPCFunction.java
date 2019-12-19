package site.jnmk.janmaki.npc_tool.api;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class NPCFunction {
    private static Map<String,NPCFunction> functions = new HashMap<>();

    private String key;

    public NPCFunction(String key){
        this.key = key;
        functions.put(key,this);
    }

    public static void run(Player player, NPC npc, String key){
        if (!functions.containsKey(key)){
            return;
        }
        NPCFunction npcFunction = functions.get(key);
        npcFunction.run(player,npc);
    }

    public String getKey(){
        return key;
    }

    public static NPCFunction getFunctionByString(String key){
        for (String str: functions.keySet()){
            if (str.equalsIgnoreCase(key)){
                return functions.get(str);
            }
        }
        return null;
    }

    public abstract void run(Player player, NPC npc);

    public static Map<String,NPCFunction> getFunctions(){
        return functions;
    }
}
