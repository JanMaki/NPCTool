package site.jnmk.janmaki.npc_tool;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import site.jnmk.janmaki.npc_tool.Listener.ClickNPCListener;
import site.jnmk.janmaki.npc_tool.Listener.PacketListener;
import site.jnmk.janmaki.npc_tool.api.NPCFunction;
import site.jnmk.janmaki.npc_tool.tpye.ClickType;
import site.jnmk.janmaki.npc_tool.tpye.SenderType;

import java.util.List;
import java.util.Objects;

public class NPCTool extends JavaPlugin{
    private CustomConfiguration nFile;
    private FileConfiguration nConfig;
    private PacketListener packetListener;

    public static NPCTool instance;

    @Override
    public void onEnable(){
        instance = this;
        Objects.requireNonNull(getCommand("npct")).setExecutor(new NPCTCommand());
        PluginManager plManager = Bukkit.getPluginManager();
        plManager.registerEvents(new ClickNPCListener(),this);
        packetListener = new PacketListener();
        plManager.registerEvents(packetListener,this);
        nFile = new CustomConfiguration(this,"npcs.yml");
        nFile.saveDefaultConfig();
        nConfig = nFile.getConfig();
        for (Player player : Bukkit.getOnlinePlayers()){
            packetListener.injectPlayer(player);
        }
    }

    @Override
    public void onDisable(){
        for (Player player : Bukkit.getOnlinePlayers()){
            packetListener.removePlayer(player);
        }
    }

    void reload(CommandSender sender){
        nFile.reloadConfig();
        nConfig = nFile.getConfig();
        sender.sendMessage(ChatColor.GREEN+"Reloaded config file!");
    }

    private NPC getSelectedNPC(Player player){
        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
        if (npc == null){
            player.sendMessage(ChatColor.GREEN+"You must have an NPC selected to execute that command.");
            return null;
        }
        return npc;
    }

    //コマンド
    void addCmd(Player player, ClickType clickType, SenderType senderType, String command){
        NPC npc = getSelectedNPC(player);
        if (npc == null){
            return;
        }
        List<String> list = getCmd(npc,clickType,senderType);
        list.add(command);
        int id = npc.getId();
        String path = id+".commands."+clickType.toString()+"."+senderType.toString();
        nConfig.set(path,list);
        nFile.saveConfig();
        player.sendMessage(ChatColor.GREEN+"Added command to "+ChatColor.YELLOW+npc.getName()+ChatColor.GREEN+".");
    }

    void removeCmd(Player player){
        NPC npc = getSelectedNPC(player);
        if (npc == null){
            return;
        }
        int id = npc.getId();
        String path = id+".commands";
        nConfig.set(path,null);
        nFile.saveConfig();
        player.sendMessage(ChatColor.GREEN+"Remove commands to "+ChatColor.YELLOW+npc.getName()+ChatColor.GREEN+".");
    }

    public void runCmd(NPC npc,ClickType clickType,Player player){
        int id = npc.getId();
        String path = id+".commands."+clickType.toString();
        List<String> playerCmd = nConfig.getStringList(path+"."+SenderType.PLAYER.toString());
        List<String> consoleCmd = nConfig.getStringList(path+"."+SenderType.CONSOLE.toString());
        for (String cmd:playerCmd){
            cmd = cmd.substring(1);
            cmd = cmd.replaceAll("%player%",player.getName());
            player.performCommand(cmd);
        }
        for (String cmd:consoleCmd){
            cmd = cmd.substring(1);
            cmd = cmd.replaceAll("%player%",player.getName());
            getServer().dispatchCommand(getServer().getConsoleSender(),cmd);
        }
    }

    private List<String> getCmd(NPC npc,ClickType clickType,SenderType senderType){
        int id = npc.getId();
        String path = id+".commands."+clickType.toString()+"."+senderType.toString();
        return nConfig.getStringList(path);
    }

    //ファンクション
    public void addFunc(Player player, NPCFunction npcFunction){
        NPC npc = getSelectedNPC(player);
        if (npc == null){
            return;
        }
        int id = npc.getId();
        String path = id+".functions";
        List<String> list = nConfig.getStringList(path);
        list.add(npcFunction.getKey());
        nConfig.set(path,list);
        nFile.saveConfig();
        player.sendMessage(ChatColor.GREEN+"Added function to "+ChatColor.YELLOW+npc.getName()+ChatColor.GREEN+".");
    }

    public void removeFunc(Player player){
        NPC npc = getSelectedNPC(player);
        if (npc == null) return;
        int id = npc.getId();
        String path = id+".functions";
        nConfig.set(path,null);
        nFile.saveConfig();
        player.sendMessage(ChatColor.GREEN+"Remove functions to "+ChatColor.YELLOW+npc.getName()+ChatColor.GREEN+".");
    }

    public void runFunc(NPC npc,Player player){
        int id = npc.getId();
        String path = id+".functions";
        List<String> list = nConfig.getStringList(path);
        for (String str:list){
            NPCFunction.run(player,npc,str);
        }
    }

    //見えなくする
    public void hideNPC(NPC npc,Player player){
        if (npc.getEntity().getType() != EntityType.PLAYER){
            return;
        }
        String uuid = player.getUniqueId().toString();
        List<String> hides = getHide(npc);
        if (!hides.contains(uuid))
            hides.add(uuid);
        int id = npc.getId();
        String path = id+".hide";
        nConfig.set(path,hides);
        nFile.saveConfig();
        //Destroy packet
        Entity entity = ((CraftEntity) npc.getEntity()).getHandle();
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getId());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void showNPC(NPC npc,Player player){
        String uuid = player.getUniqueId().toString();
        List<String> hides = getHide(npc);
        hides.remove(uuid);
        int id = npc.getId();
        String path = id+".hide";
        nConfig.set(path,hides);
        nFile.saveConfig();
        //Spawn Packet
        npc.despawn();
        npc.spawn(npc.getStoredLocation());
    }

    public List<String> getHide(NPC npc){
        int id = npc.getId();
        String path = id+".hide";
        return nConfig.getStringList(path);
    }
}
