package de.igelstudios.igeladdon;

import de.igelstudios.igeladdon.commands.APT;
import de.igelstudios.igeladdon.commands.InfoCommand;
import de.igelstudios.igeladdon.commands.PreGen;
import de.igelstudios.igeladdon.commands.RamUsage;
import de.igelstudios.igeladdon.event.ChatEvent;
import de.igelstudios.igeladdon.event.DeathEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class IgelAddon extends JavaPlugin {
    private static IgelAddon plugin;
    private InfoCommand info;
    private ChatEvent chat;
    private APT apt;

    @Override
    public void onEnable() {
        plugin = this;
        RamUsage usage = new RamUsage();
        info = new InfoCommand();
        info.enable();
        this.getCommand("server-info").setExecutor(usage);
        this.getCommand("server-info").setTabCompleter(usage);
        PreGen gen = new PreGen();
        this.getCommand("gen").setExecutor(gen);
        this.getCommand("gen").setTabCompleter(gen);
        apt = new APT();
        this.getCommand("apt").setExecutor(apt);
        this.getCommand("apt").setTabCompleter(apt);
        chat = new ChatEvent();
        Bukkit.getPluginManager().registerEvents(chat,this);
        Bukkit.getPluginManager().registerEvents(new DeathEvent(),this);
    }

    @Override
    public void onDisable() {
        info.disable();
        apt.save();
    }

    public void sendToOps(Component msg){
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(player.isOp())player.sendMessage(msg);
        });
    }

    public static IgelAddon getPlugin() {
        return plugin;
    }

    public ChatEvent getChat() {
        return chat;
    }
}
