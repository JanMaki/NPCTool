package site.jnmk.janmaki.npc_tool.Listener;

import io.netty.channel.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import site.jnmk.janmaki.npc_tool.NPCTool;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class PacketListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        injectPlayer(event.getPlayer());
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        removePlayer(event.getPlayer());
    }

    public void removePlayer(Player player){
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(()-> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    public void injectPlayer(Player player){
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                if(packet instanceof PacketPlayOutNamedEntitySpawn){
                    PacketPlayOutNamedEntitySpawn spawnPacket = (PacketPlayOutNamedEntitySpawn) packet;
                    UUID uuid;
                    try {
                        Field f = spawnPacket.getClass().getDeclaredField("b");
                        f.setAccessible(true);
                        uuid = (UUID) f.get(spawnPacket);
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e){
                        e.printStackTrace();
                        return;
                    }
                    Entity entity = Bukkit.getEntity(uuid);
                    if  (CitizensAPI.getNPCRegistry().isNPC(entity) ){
                        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
                        List<String> hides = NPCTool.instance.getHide(npc);
                        String playerUUID = player.getUniqueId().toString();
                        if (hides.contains(playerUUID)) {
                            return;
                        }
                    }
                }
                super.write(channelHandlerContext,packet,channelPromise);
            }

        };
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler",player.getName(),channelDuplexHandler);
    }
}
