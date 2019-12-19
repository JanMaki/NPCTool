package site.jnmk.janmaki.npc_tool;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import site.jnmk.janmaki.npc_tool.api.NPCFunction;
import site.jnmk.janmaki.npc_tool.tpye.ClickType;
import site.jnmk.janmaki.npc_tool.tpye.SenderType;

import javax.annotation.Nonnull;

public class NPCTCommand implements CommandExecutor {
    private String help = ChatColor.YELLOW+"=====[ "+ChatColor.AQUA+"NPCT Help "+ChatColor.WHITE+"1/1"+ChatColor.YELLOW+" ]=====\n"+
            ChatColor.GRAY+"/"+ChatColor.RED+"npct addCmd (--sender [player console]) (--click [all right left]) [command]"+ChatColor.GRAY+" - "+ChatColor.YELLOW+"Add NPCs command\n"+
            ChatColor.GRAY+"/"+ChatColor.RED+"npct removeCmd"+ChatColor.GRAY+" - "+ChatColor.YELLOW+"Remove NPCs commands\n"+
            ChatColor.GRAY+"/"+ChatColor.RED+"npct addFunc [FunctionKey]"+ChatColor.GRAY+" - "+ChatColor.YELLOW+"Add NPCs function\n"+
            ChatColor.GRAY+"/"+ChatColor.RED+"npct removeFunc"+ChatColor.GRAY+" - "+ChatColor.YELLOW+"Remove NPCs functions\n"+
            ChatColor.GRAY+"/"+ChatColor.RED+"npct funcList"+ChatColor.GRAY+" - "+ChatColor.YELLOW+"Get functions list\n"+
            ChatColor.GRAY+"/"+ChatColor.RED+"npct reload"+ChatColor.GRAY+" - "+ChatColor.YELLOW+"Reload config files\n";
    private NPCTool npcTool;

    NPCTCommand(){
        npcTool = NPCTool.instance;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!(sender instanceof Player)){
            if (args.length >= 1 && args[0].equalsIgnoreCase("reload")){
                npcTool.reload(sender);
            }
            sender.sendMessage(ChatColor.RED+"Its player only.");
            return true;
        }
        if (args.length < 1){
            sender.sendMessage(help);
            return true;
        }
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("addCmd")){
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED+"/npct addCmd (--sender [player console]) (--click [all right left]) [command]");
                return true;
            }
            StringBuilder cmd = new StringBuilder();
            boolean startCmd = false;
            SenderType senderType = SenderType.PLAYER;
            ClickType clickType = ClickType.ALL;
            for (int i = 1; i < args.length; i++){
                String str = args[i];
                if (startCmd){
                    cmd.append(" "+str);
                    continue;
                }
                if (str.contains("/")){
                    startCmd = true;
                    cmd.append(str);
                    continue;
                }
                if (str.equalsIgnoreCase("--sender")){
                    if (args.length < i+2) {
                        player.sendMessage(ChatColor.RED+"/npct addCmd (--sender [player console]) (--click [all right left]) [command]");
                        return true;
                    }
                    senderType = SenderType.getSenderTypeByString(args[i+1]);
                    continue;
                }
                if (str.equalsIgnoreCase("--click")){
                    if (args.length < i+2) {
                        player.sendMessage(ChatColor.RED+"/npct addCmd (--sender [player console]) (--click [all right left]) [command]");
                        return true;
                    }
                    clickType = ClickType.getClickTypeByString(args[i+1]);
                    continue;
                }
            }
            if (senderType == null || clickType == null || cmd.toString().equalsIgnoreCase("")){
                player.sendMessage(ChatColor.RED+"/npct addCmd (--sender [player console]) (--click [all right left]) [command]");
                return true;
            }
            NPCTool.instance.addCmd(player,clickType,senderType,cmd.toString());
            return true;
        }
        if (args[0].equalsIgnoreCase("removeCmd")){
            npcTool.removeCmd(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("addFunc")){
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED+"/npct addFunc [FunctionKey]");
                return true;
            }
            String key = args[1];
            NPCFunction function = NPCFunction.getFunctionByString(key);
            if (function == null) {
                player.sendMessage(ChatColor.RED+"Unknown function!");
                return true;
            }
            NPCTool.instance.addFunc(player,function);
            return true;
        }
        if (args[0].equalsIgnoreCase("removeFunc")){
            npcTool.removeFunc(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("funcList") || args[0].equalsIgnoreCase("funcsList") ){
            player.sendMessage(ChatColor.YELLOW+"=====[ "+ChatColor.GREEN+"Functions"+ChatColor.YELLOW+" ]=====");
            for (String str:NPCFunction.getFunctions().keySet()){
                player.sendMessage(ChatColor.AQUA+str);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")){
            npcTool.reload(sender);
            return true;
        }
        player.sendMessage(help);
        return true;
    }
}
