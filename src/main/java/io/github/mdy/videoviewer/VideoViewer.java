package io.github.mdy.videoviewer;

import io.github.mdy.videoviewer.commands.CommandScreen;
import io.github.mdy.videoviewer.commands.MainTabCompleter;
import io.github.mdy.videoviewer.playback.VideoDecoder;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Level;
import java.util.logging.LogManager;

@Plugin(name="VideoViewer", version="0.1")
@Description("A Plugin for creating video screens and playing videos on them.")
@Author("m;dy")
@Website("md-y.github.io")
@Permission(name="videoviewer.*", desc="Permissions for Video Viewer", defaultValue= PermissionDefault.OP, children={
        @ChildPermission(name="videoviewer.screen")
})
@Commands(
        @Command(
                name="screen",
                desc="Create and delete screens. The nearest screen will be selected.",
                usage=  "/<command> [select|destroy]\n" +
                        "/<command> create [width] [height] [type]\n" +
                        "/<command> play [video]\n" +
                        "/<command> workers [list|purge]\n",
                permission="videoviewer.command.screen"
        )
)
public class VideoViewer extends JavaPlugin {
    public static JavaPlugin instance;

    @Override
    public void onEnable() {
        VideoViewer.instance = this; // Needs to be first
        VideoViewer.log("VideoViewer Enabled!");

        Bukkit.getScheduler().cancelTasks(this); // Stop tasks on reload

        MainTabCompleter mtc = new MainTabCompleter();
        this.getCommand("screen").setExecutor(new CommandScreen());
        this.getCommand("screen").setTabCompleter(mtc);

        // Mute warning given by compressed jars
        Bukkit.getLogger().log(Level.INFO, "test");
    }

    @Override
    public void onDisable() {
        VideoDecoder.clearCache();
        VideoViewer.log("Cleared cache.");
        VideoViewer.log("VideoViewer Disabled!");
    }

    public static void log(Level level, Object... text) {
        String[] stringArray = new String[text.length];
        for (int i = 0; i < text.length; i++) stringArray[i] = String.valueOf(text[i]);
        String finalText = String.join(" ", stringArray);
        VideoViewer.instance.getLogger().log(Level.INFO, finalText);
    }

    public static void log(Object... text) {
        VideoViewer.log(Level.INFO, text);
    }
}
