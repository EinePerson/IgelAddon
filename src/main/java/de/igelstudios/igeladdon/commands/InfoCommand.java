package de.igelstudios.igeladdon.commands;

import de.igelstudios.igeladdon.IgelAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InfoCommand implements CommandExecutor, TabCompleter {
    private final Map<Character,TextColor> cols = map();
    List<Component> msg;
    String txt;
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args != null && args.length >= 2){
            if(!sender.isOp()){
                sender.sendMessage(Component.text("No rights!").color(NamedTextColor.RED));
                return true;
            }else if(args[0].equals("set")){
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    builder.append(args[i]);
                }
                txt = builder.toString();
                parse();
            }else if(args[0].equals("add")){
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    builder.append(args[i]);
                }
                txt += builder.toString();
                parse();
            }
        }
        msg.forEach(sender::sendMessage);
        return true;
    }

    private void parse(){
        msg = new ArrayList<>();

        char[] c = txt.toCharArray();
        int j = 0;
        for (int i = 0; i < c.length - 1; i++) {
            if(c[i] == '\\' && c[i + 1] == 'n'){
                msg.add(Component.text(new String(c,j,i-j)).color(NamedTextColor.WHITE));
                i += 2;
                j = i;
            }
        }
        msg.add(Component.text(new String(c,j,c.length-j)));
    }

    private TextColor col(char c){
        TextColor tc = cols.get(c);
        return tc == null ? NamedTextColor.WHITE:tc;
    }

    private Map<Character,TextColor> map(){
        Map<Character,TextColor> m = new HashMap<>();
        m.put('0', NamedTextColor.BLACK);
        m.put('1', NamedTextColor.BLUE);
        m.put('2', NamedTextColor.GREEN);
        m.put('3', NamedTextColor.AQUA);
        m.put('4', NamedTextColor.DARK_RED);
        m.put('5', NamedTextColor.LIGHT_PURPLE);
        m.put('6', NamedTextColor.YELLOW);
        m.put('7', NamedTextColor.GRAY);
        m.put('8', NamedTextColor.DARK_GRAY);
        m.put('9', NamedTextColor.DARK_PURPLE);
        m.put('a', TextColor.color(0x00FF00));
        m.put('b', NamedTextColor.AQUA);
        m.put('c', NamedTextColor.RED);
        m.put('d', TextColor.color(0x800080));
        m.put('e', NamedTextColor.YELLOW);
        m.put('f', NamedTextColor.WHITE);
        return m;
    }

    public void enable(){
        try {
            File f = new File(IgelAddon.getPlugin().getDataFolder().getAbsolutePath() + "/info.txt");
            if(!f.exists())f.createNewFile();
            txt = new String(Files.readAllBytes(Path.of(IgelAddon.getPlugin().getDataFolder().getAbsolutePath() + "/info.txt")));
            parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disable(){
        try {
            Files.writeString(Path.of( IgelAddon.getPlugin().getDataFolder().getAbsolutePath() + "/info.txt"),txt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!commandSender.isOp())return null;
        if(args.length == 1)return List.of("set","add");
        else return List.of("<info>");
    }
}
