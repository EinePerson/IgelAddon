package de.igelstudios.igeladdon.commands;

import de.igelstudios.igeladdon.Config;
import de.igelstudios.igeladdon.IgelAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PreGen implements CommandExecutor, TabCompleter {
    protected boolean generating;
    protected Gen thread;
    protected Config config;

    public PreGen(){
        config = new Config(IgelAddon.getPlugin(),"congig.yml");
        Map<String,Object> border = config.getConfig().getValues(false);
        for (World world : Bukkit.getWorlds()) {
            if(!border.containsKey(world.getName()))continue;
            try {
                if(!(config.getConfig().contains(world.getName() + "_gen") && config.getConfig().getBoolean(world.getName() + "_gen"))) {
                    int i = Integer.parseInt(String.valueOf(border.get(world.getName())));
                    gen(world, i);
                    world.getWorldBorder().setSize(i * 16);
                }
            }catch (NumberFormatException e){
                Bukkit.getLogger().warning(border.get(world.getName()) + " should be a number");
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(Component.text("Only Players can use this command").color(TextColor.color(Color.RED.asRGB())));
            return true;
        }
        if(args.length == 1) {
            if(args[0].equals("progress")){
                if(!generating)sender.sendMessage(Component.text("No Chunks in generation").color(NamedTextColor.RED));
                else{
                    Component component = Component.text("Chunks to generate: ").color(NamedTextColor.YELLOW);
                    component = component.append(Component.text(thread.l).color(NamedTextColor.RED));
                    component = component.append(Component.text("\nChunks left to generate: ").color(NamedTextColor.YELLOW));
                    component = component.append(Component.text(thread.k).color(NamedTextColor.RED));
                    component = component.append(Component.text("\nProgress: ").color(NamedTextColor.YELLOW));
                    component = component.append(Component.text(String.format("%.3f", 100f - thread.k * 100f / (float) thread.l) + "%").color(NamedTextColor.RED));
                    sender.sendMessage(component);
                }
            }else {
                if (generating) sender.sendMessage(Component.text("Already generating chunks").color(NamedTextColor.RED));
                try {
                    int radius = Integer.parseInt(args[0]);
                    radius /= 16;
                    radius = Math.abs(radius);
                    gen(((Player) sender).getWorld(), radius);
                    config.getConfig().set(((Player) sender).getWorld().getName(), radius);
                    config.save();
                    IgelAddon.getPlugin().sendToOps(Component.text("Generating chunks with a radius of " + radius * 16 + " Blocks, a total of " + (radius * radius * 4 + radius * 4 + 1) + " chunks, in " + ((Player) sender)
                            .getWorld().getName()).color(TextColor.color(Color.LIME.asRGB())));
                    Bukkit.getLogger().info("Generating chunks with a radius of " + radius * 16 + " Blocks, a total of " + (radius * radius * 4 + radius * 4 + 1) + " chunks, in " + ((Player) sender).getWorld().getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("1st argument (radius) needs to be a number").color(TextColor.color(Color.RED.asRGB())));
                }
            }
        }else if(args.length == 2) {
            Chunk chunk = ((Player) sender).getWorld().getChunkAt(Integer.parseInt(args[0]),Integer.parseInt(args[1]),false);
            sender.sendPlainMessage("Is generated " + chunk.isGenerated() + ":" + chunk.isLoaded());
        }else{
            sender.sendMessage(Component.text("Generates every chunk in the specified radius (as Square)").color(TextColor.color(Color.AQUA.asRGB())));
        }
        return true;
    }

    public void gen(World world,int radius){
        world.getWorldBorder().setSize(radius * 16);
        config.getConfig().set(world.getName() + "_gen", false);
        config.save();
        generating = true;

        thread = new Gen(world, radius);
        thread.runTaskTimer(IgelAddon.getPlugin(),0L,1L);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("<radius>","progress");
    }

    public class Gen extends BukkitRunnable{
        private int radius;
        private World world;
        int i;
        int j;
        final int l;
        int k;

        private Gen(World world,int radius){
            this.radius = radius;
            this.world = world;
            i = -radius;
            j = -radius;
            l = radius * radius * 4 + radius * 4 + 1;
            k = l;
        }

        @Override
            public void run(){
            for (int m = 0; m < 5; m++) {
                    if(j > radius){
                        i++;
                        j = -radius;
                    }
                    if(i > radius){
                        cancel();
                        break;
                    }
                    world.getChunkAtAsync(i, j, true).thenAccept(chunk -> {
                        k--;
                        if(k == 0){
                            IgelAddon.getPlugin().sendToOps(Component.text("Finished generating chunks in " + world.getName()).color(TextColor.color(Color.LIME.asRGB())));
                            Bukkit.getLogger().info("Finished generating chunks in " + world.getName());
                            generating = false;
                            config.getConfig().set(world.getName() + "_gen", true);
                            config.save();
                        }
                    });
                    j++;
                }
        }
    }
}
