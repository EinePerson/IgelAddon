package de.igelstudios.igeladdon.event;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

public class ChatEvent implements Listener {

    @EventHandler
    public void chat(AsyncChatEvent e){
        e.setCancelled(true);
        sendAs(e.getPlayer(),e.message());
        if(true)return;
        StringBuilder msg = new StringBuilder();
        try {
            Field field = e.message().getClass().getDeclaredField("content");
            field.trySetAccessible();
            msg.append(field.get(e.message()));
            for (Component child : e.message().children()) {
                msg.append(field.get(child));
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        Bukkit.getLogger().info(msg.toString());

    }

    public void sendAs(Player p,Component msg){
        Component component = Component.text(p.getName() + ": ").color(p.isOp() ? NamedTextColor.RED:NamedTextColor.GREEN);
        component = component.append(msg.color(NamedTextColor.WHITE));
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(component);
        }
    }

    public void sendAs(Player p,String msg){
        sendAs(p,Component.text(msg).color(NamedTextColor.WHITE));
    }
}
