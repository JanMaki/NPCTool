package site.jnmk.janmaki.npc_tool.Listener;

import net.citizensnpcs.api.event.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import site.jnmk.janmaki.npc_tool.tpye.ClickType;
import site.jnmk.janmaki.npc_tool.NPCTool;

import java.util.HashSet;
import java.util.Set;

public class ClickNPCListener implements Listener {
    private Set<Player> cool = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeftClick(NPCLeftClickEvent event) {
        if (cool.contains(event.getClicker())) {
            return;
        }
        cool.add(event.getClicker());
        NPCTool.instance.runCmd(event.getNPC(), ClickType.LEFT, event.getClicker());
        NPCTool.instance.runCmd(event.getNPC(), ClickType.ALL, event.getClicker());
        NPCTool.instance.runFunc(event.getNPC(), event.getClicker());
        new BukkitRunnable() {
            @Override
            public void run() {
                cool.remove(event.getClicker());
            }
        }.runTaskLater(NPCTool.instance, 10);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(NPCRightClickEvent event){
        if (cool.contains(event.getClicker())){
            return;
        }
        cool.add(event.getClicker());
        NPCTool.instance.runCmd(event.getNPC(), ClickType.RIGHT,event.getClicker());
        NPCTool.instance.runCmd(event.getNPC(), ClickType.ALL,event.getClicker());
        NPCTool.instance.runFunc(event.getNPC(),event.getClicker());
        new BukkitRunnable() {
            @Override
            public void run() {
                cool.remove(event.getClicker());
            }
        }.runTaskLater(NPCTool.instance,10);
    }
}
