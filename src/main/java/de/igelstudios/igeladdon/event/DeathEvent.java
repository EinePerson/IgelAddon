package de.igelstudios.igeladdon.event;

import de.igelstudios.igeladdon.Config;
import de.igelstudios.igeladdon.IgelAddon;
import it.unimi.dsi.fastutil.floats.Float2CharLinkedOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DeathEvent implements Listener {
    private Map<Location, List<ItemStack>> graves;
    private Config config;

    public DeathEvent(){

    }

    @EventHandler
    public void death(PlayerDeathEvent e){
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(e.deathMessage());
            if(!e.getPlayer().equals(player))IgelAddon.getPlugin().getChat().sendAs(player,"f");
        });
        e.deathMessage(Component.text(""));
        if(e.getDrops().size() == 0)return;
        Location loc = e.getPlayer().getLocation().toBlockLocation();
        if(loc.getWorld().getEnvironment().equals(World.Environment.THE_END)) loc.setY(Math.max(loc.getY(),20));
        loc.getBlock().setType(Material.CHEST);
        Chest chest = ((Chest) loc.getBlock().getState());
        for (int i = 0; i < Math.min(e.getDrops().size(), 27); i++) {
            chest.getBlockInventory().addItem(e.getDrops().get(i));
        }
        e.getPlayer().sendMessage(Component.text("You died at: " + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getWorld().getName()).color(NamedTextColor.RED));
        if(e.getDrops().size() < 27){
            e.getDrops().clear();
            return;
        }
        loc.setY(loc.getBlockY() + 1);
        loc.getBlock().setType(Material.CHEST);
        chest = ((Chest) loc.getBlock().getState());
        for (int i = 27; i < e.getDrops().size(); i++) {
            chest.getBlockInventory().addItem(e.getDrops().get(i));
        }
        e.getDrops().clear();
    }
}
