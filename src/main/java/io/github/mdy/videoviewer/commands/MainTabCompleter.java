package io.github.mdy.videoviewer.commands;

import io.github.mdy.videoviewer.VideoViewer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        // Only the screen command is used. This also means this class isn't needed, but whatever
        if (!label.equals("screen")) return null;

        List<String> options = new ArrayList<String>();

        if (args.length == 1) {
            options.add("create");
            options.add("destroy");
            options.add("select");
            options.add("play");
            options.add("stop");
            options.add("pause");
            options.add("workers");
        }

        if (args[0].equals("play") && args.length > 1) {
            File[] files = VideoViewer.instance.getDataFolder().listFiles();
            if (files != null) for (File f : files) options.add(f.getName());
        }

        if (args[0].equals("create") && args.length > 3) {
            options.add("ARMOR_STAND");
            options.add("SHEEP");
        }

        if (args[0].equals("workers") && args.length > 1) {
            options.add("list");
            options.add("purge");
        }

        // Remove unneeded options
        if (args[args.length - 1].length() > 0) {
            for (int i = options.size() - 1; i >= 0; i--) {
                if (!options.get(i).startsWith(args[args.length - 1].toLowerCase())) options.remove(i);
            }
        }

        return options;
    }
}
