package de.igelstudios.igeladdon.commands;

import de.igelstudios.igeladdon.IgelAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InfoCommand implements CommandExecutor {
    private final Map<Character,TextColor> cols = map();
    List<Component> msg;
    String txt;
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 2){
            if(!sender.isOp()){
                sender.sendMessage(Component.text("No rights!").color(NamedTextColor.RED));
                return true;
            }else if(args[0].equals("set")){
                txt = args[1];
                parse();
            }else if(args[0].equals("add")){
                txt += args[1];
                parse();
            }
        }
        msg.forEach(sender::sendMessage);
        return true;
    }

    private void parse(){
        msg = new ArrayList<>();
        String text = "";
        text += txt + "$f";
        text = text.replace('_',' ');

        char[] c = text.toCharArray();
        int j = 0;
        for (int i = 0; i < c.length; i++) {
            if(c[i] == '\n'){
                msg.add(parse(new String(c,j,i-j)));
                j = i;
            }
        }
    }

    private Component parse(String txt){
        char[] c = txt.toCharArray();
        Component comp = Component.text("");
        List<String> colorsSt = new ArrayList<>();
        List<TextColor> colorTx = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < c.length; i++) {
            if(c[i] == '$'){
                i++;
                colorsSt.add(new String(c,j,i - j - 1));
                colorTx.add(col(c[i++]));
                j = i;
            }
        }

        for (int i = 0;i < colorsSt.size();i++) {
            String s = colorsSt.get(i);
            TextColor textColor = colorTx.get(i);
            comp = comp.append(Component.text(s).color(textColor));
        }
        return comp;
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
}
