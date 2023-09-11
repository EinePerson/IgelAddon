package de.igelstudios.igeladdon.commands;

import com.sun.management.OperatingSystemMXBean;
import de.igelstudios.igeladdon.IgelAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;


public class RamUsage implements CommandExecutor, TabCompleter {
    int ticks = 0;
    int tps = 0;
    long t = 0;
    private BukkitTask runnable;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Component component = Component.text("");

        if(strings.length == 0){
            component = component.append(Component.text("---Server Monitor---").color(TextColor.color(Color.AQUA.asRGB())));
            component = mem(component);
            component = component.append(Component.text("\n "));
            component = meta(component);
            component = component.append(Component.text("\n "));
            component = cpu(component);
            component = component.append(Component.text("\n "));
            component = game(component);
        }else {
            component = component.append(Component.text("---Server Monitor---").color(TextColor.color(Color.AQUA.asRGB())));
            for (String string : strings) {
                switch (string) {
                    case "mem" -> component = mem(component);
                    case "meta" -> component = meta(component);
                    case "cpu" -> component = cpu(component);
                    case "game" -> component = game(component);
                    case "info" -> component = info(component);
                    default -> component = err(component, string);
                }
            }
        }

        commandSender.sendMessage(component);
        return true;
    }

    private Component err(Component component,String inv){
        component = component.append(Component.text("---Error---").color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nUnexpected Symbol").color(TextColor.color(Color.ORANGE.asRGB())));
        return component.append(Component.text("\n" + inv).color(TextColor.color(Color.RED.asRGB())));
    }

    private Component info(Component component){
        component = component.append(Component.text("---Info---").color(TextColor.color(Color.LIME.asRGB())));
        component = component.append(Component.text("\nmem: Memory Info").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text("\nmeta: Memory Meta Info").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text("\ncpu: CPU Usage").color(TextColor.color(Color.ORANGE.asRGB())));
        return component.append(Component.text("\ngame: Loaded Chunks and Entities").color(TextColor.color(Color.ORANGE.asRGB())));
    }

    private Component mem(Component component){
        MemoryUsage memUsg = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long maxMemory   = memUsg.getMax();
        long totalMemory = memUsg.getCommitted();
        long usedMemory = memUsg.getUsed();
        long free = maxMemory - usedMemory;

        component = component.append(Component.text("\n---Memory---").color(TextColor.color(Color.LIME.asRGB())));
        component = component.append(Component.text("\nUsed: ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(usedMemory/1048576L + " MB (" + (usedMemory*100)/maxMemory + "%)").color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nFree: ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(free/1048576L        + " MB").color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nAlloc: ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(totalMemory/1048576L + " MB").color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nMax: ").color(TextColor.color(Color.ORANGE.asRGB())));
        return component.append(Component.text(maxMemory/1048576L + " MB").color(TextColor.color(Color.RED.asRGB())));
    }

    private Component meta(Component component){
        long usedMetaspace  = -1;
        long allocMetaspace = -1;
        for (MemoryPoolMXBean mem : ManagementFactory.getMemoryPoolMXBeans())
        {
            if ("Metaspace".equals(mem.getName()))
            {
                usedMetaspace = mem.getUsage().getUsed();
                allocMetaspace = mem.getUsage().getCommitted();
                break;
            }
        }

        long freeMetaspace = allocMetaspace - usedMetaspace;

        component = component.append(Component.text("\n---MetaSpace---").color(TextColor.color(Color.LIME.asRGB())));
        component = component.append(Component.text("\nUsed: ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(usedMetaspace/1048576L + " MB (" + (usedMetaspace*100)/allocMetaspace + "%)").color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nFree: ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(freeMetaspace / 11048576L        + " MB").color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nMax: ").color(TextColor.color(Color.ORANGE.asRGB())));
        return component.append(Component.text(allocMetaspace / 1048576L + " MB").color(TextColor.color(Color.RED.asRGB())));
    }

    private Component cpu(Component component){
        OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double cpu = os.getProcessCpuLoad();

        component = component.append(Component.text("\n---CPU---").color(TextColor.color(Color.LIME.asRGB())));
        component = component.append(Component.text("\nUsed: ").color(TextColor.color(Color.ORANGE.asRGB())));
        return component.append(Component.text(String.format("%.3f", cpu * 100) + "%").color(TextColor.color(Color.RED.asRGB())));
    }

    private Component game(Component component){
        component = component.append(Component.text("\n---Minecraft---").color(TextColor.color(Color.LIME.asRGB())));
        component = component.append(Component.text("\nTPS:   ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(String.valueOf(tps)).color(TextColor.color(Color.RED.asRGB())));

        int i = 0;
        int j = 0;
        for (World world : Bukkit.getWorlds()) {
            int chunks = world.getLoadedChunks().length;
            i += chunks;
            int entities = world.getEntityCount();
            j += entities;
            component = component.append(Component.text("\n---" + world.getName()).color(TextColor.color(Color.BLUE.asRGB())));
            component = component.append(Component.text("\nLoaded Chuncks: ").color(TextColor.color(Color.ORANGE.asRGB())));
            component = component.append(Component.text(String.valueOf(chunks)).color(TextColor.color(Color.RED.asRGB())));
            component = component.append(Component.text("\nEntities: ").color(TextColor.color(Color.ORANGE.asRGB())));
            component = component.append(Component.text(String.valueOf(entities)).color(TextColor.color(Color.RED.asRGB())));
            world.getChunkAt(0,0,true);
        }

        component = component.append(Component.text("\n---All").color(TextColor.color(Color.BLUE.asRGB())));
        component = component.append(Component.text("\nLoaded Chuncks: ").color(TextColor.color(Color.ORANGE.asRGB())));
        component = component.append(Component.text(String.valueOf(i)).color(TextColor.color(Color.RED.asRGB())));
        component = component.append(Component.text("\nEntities: ").color(TextColor.color(Color.ORANGE.asRGB())));
        return component.append(Component.text(String.valueOf(j)).color(TextColor.color(Color.RED.asRGB())));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of("info","mem","meta","cpu","game");
    }

    public RamUsage(){
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                ticks++;
                if(t == 0)t = System.currentTimeMillis();
                if(System.currentTimeMillis() >=  t + 1000){
                    tps = ticks;
                    ticks = 0;
                    t = System.currentTimeMillis();
                }
            }
        }.runTaskTimer(IgelAddon.getPlugin(),0L, 1L);
    }
}
