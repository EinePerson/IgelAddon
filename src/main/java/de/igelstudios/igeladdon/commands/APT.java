package de.igelstudios.igeladdon.commands;

import de.igelstudios.igeladdon.Config;
import de.igelstudios.igeladdon.IgelAddon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APT implements CommandExecutor, TabCompleter {
    Config config;
    Map<String,String> modules;

    public APT(){
        config = new Config(IgelAddon.getPlugin(),"apt.yml");
        modules = new HashMap<>();
        config.getConfig().getKeys(false).forEach(key -> modules.put(key,config.getConfig().getString(key)));
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0)return false;
        if(!sender.isOp())return false;
        switch (args[0]) {
            case "update" -> {
                get();
                sender.sendMessage(Component.text("Successfully updated every module").color(NamedTextColor.GREEN));
                return true;
            }
            case "add" -> {
                if(args.length < 3){
                    sender.sendMessage(Component.text("Required are name and url").color(NamedTextColor.RED));
                    return true;
                }
                modules.put(args[1],args[2]);
                sender.sendMessage(Component.text("Successfully added " + args[1]).color(NamedTextColor.GREEN));
            }
            case "remove" -> {
                if(args.length < 2){
                    sender.sendMessage(Component.text("Required is a name").color(NamedTextColor.RED));
                    return true;
                }
                modules.remove(args[1]);
                sender.sendMessage(Component.text("Successfully removed " + args[1]).color(NamedTextColor.GREEN));
                return true;
            }
            case "list" -> {
                send(sender);
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1)return List.of("update","add","remove","list");
        else {
            switch (args[0]) {
                case "add" ->{
                    if(args.length == 2)return List.of("<name>");
                    else if(args.length == 3)return List.of("<url>");
                }
                case "remove" -> {
                    if(args.length == 2)return List.of("<name>");
                }
                case "list","update" -> {
                    return List.of("");
                }
            }
        }
        return List.of("");
    }

    public void save(){
        config.getConfig().getKeys(false).forEach(key -> {if(!modules.containsKey(key))config.getConfig().set(key,null);});
        modules.forEach((key,mod) -> config.getConfig().set(key,mod));
        config.save();
    }

    private void send(Audience audience){
        audience.sendMessage(Component.text("---Module name: Module URL---").color(NamedTextColor.GREEN));
        modules.forEach((key,module) -> audience.sendMessage(Component.text(key + ": ").color(NamedTextColor.GREEN).append(Component.text(module).color(NamedTextColor.RED))));
    }

    private void get(){
        modules.forEach((key,module) -> {
            try(InputStream stream = new URL(module).openStream()) {
                File jar = new File(Config.PLUGINS + key + ".jar");
                Bukkit.getLogger().info(jar.getAbsolutePath());
                if(!jar.createNewFile())throw new RuntimeException("Could not create file: " + jar.getAbsolutePath());
                Files.write(jar.toPath(),stream.readAllBytes());
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        });
    }
}
