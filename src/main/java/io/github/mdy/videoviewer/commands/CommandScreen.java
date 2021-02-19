package io.github.mdy.videoviewer.commands;

import io.github.mdy.videoviewer.VideoViewer;
import io.github.mdy.videoviewer.playback.PlaybackWorker;
import io.github.mdy.videoviewer.screen.ScreenObject;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class CommandScreen implements CommandExecutor {

    private static final HashMap<UUID, ScreenObject> selectedScreens = new HashMap<UUID, ScreenObject>();
    private static final HashMap<ScreenObject, PlaybackWorker> currentWorkers = new HashMap<ScreenObject, PlaybackWorker>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) return false;
        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);

        // Check if workers can be purged since every command would like to know
        for (ScreenObject screen : currentWorkers.keySet())
            if (getScreenWorker(screen).isState(PlaybackWorker.State.STOPPED))
                removeScreenWorker(screen);

        // List command can be done by the console so this is called at the beginning
        if (args[0].equalsIgnoreCase("workers")) {
            return this.modifyWorkersCommand(sender, subCommandArgs);
        }

        // Player only commands
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to modify screens.");
            return true;
        }
        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "create":
                return this.createScreenCommand(player, subCommandArgs);
            case "select":
                return this.selectScreenCommand(player, subCommandArgs);
            case "destroy":
                return this.deleteScreenCommand(player, subCommandArgs);
            case "play":
                return this.playVideoCommand(player, subCommandArgs);
            case "stop":
                return this.stopVideoCommand(player, subCommandArgs);
            case "pause":
                return this.pauseVideoCommand(player, subCommandArgs);
            default:
                return false;
        }
    }

    private boolean modifyWorkersCommand(CommandSender sender, String[] args) {
        if (args.length < 1) return false;

        int workerCount = currentWorkers.size();

        switch (args[0]) {
            case "list":
                StringBuilder builder = new StringBuilder();
                builder.append("There are ").append(workerCount).append(" workers.");
                currentWorkers.forEach((k, v) ->
                        builder.append("\n").append(k.getId()).append(" - ").append(v.getFileName())
                );
                sender.sendMessage(builder.toString());
                break;
            case "purge":
                currentWorkers.values().forEach(PlaybackWorker::stop);
                sender.sendMessage("Purged " + workerCount + " worker(s).");
                break;
        }

        return true;
    }

    private boolean createScreenCommand(Player player, String[] args) {
        if (args.length < 3) return false;

        int width, height;
        EntityType type;
        try {
            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            type = EntityType.valueOf(args[2].toUpperCase());
        } catch (NumberFormatException e) {
            return false;
        } catch (IllegalArgumentException e) {
            player.sendMessage("Unknown screen type: " + args[2]);
            return true;
        }

        ScreenObject screen = new ScreenObject(width, height);
        screen.build(player.getLocation(), type);
        setPlayerScreen(player, screen);
        player.sendMessage("Created screen with id " + screen.getId());
        return true;
    }

    private boolean selectScreenCommand(Player player, String[] args) {
        ScreenObject screen = ScreenObject.findClosestLoadedScreen(player.getLocation());
        if (screen == null) {
            player.sendMessage("Could not find a valid screen that is loaded.");
            return true;
        }

        player.sendMessage("Selected screen with id " + screen.getId());
        setPlayerScreen(player, screen);
        return true;
    }

    private boolean deleteScreenCommand(Player player, String[] args) {
        if (!playerHasScreen(player)) {
            player.sendMessage("No screen is currently selected!");
            return true;
        }

        ScreenObject screen = getPlayerScreen(player);
        screen.delete();
        if (screenHasWorker(screen)) getScreenWorker(screen).stop();
        removePlayerScreen(player);
        player.sendMessage("Deleted selected screen.");
        return true;
    }

    private boolean playVideoCommand(Player player, String[] args) {
        if (args.length < 1) return false;

        if (!playerHasScreen(player)) {
            player.sendMessage("No screen is currently selected!");
            return true;
        }

        ScreenObject screen = getPlayerScreen(player);
        String fileName = String.join(" ", args);
        Path filePath = Paths.get(VideoViewer.instance.getDataFolder().getAbsolutePath(), fileName);
        if (!Files.exists(filePath)) {
            player.sendMessage("This file doesn't exist!");
            return true;
        } else if (getScreenWorker(screen) != null) {
            player.sendMessage("This screen is already playing or working!");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(VideoViewer.instance, task -> {
            PlaybackWorker worker = new PlaybackWorker(screen);
            VideoViewer.log("Reading and processing file: ", filePath);
            player.sendMessage("Reading and processing file: " + fileName);
            setScreenWorker(screen, worker);
            if (worker.loadVideo(filePath.toString())) {
                worker.start();
                player.sendMessage("Starting playback: " + args[0]);
                VideoViewer.log("Starting playback: ", filePath);
            } else {
                removeScreenWorker(screen);
                VideoViewer.log("Video loading failed or stopped: ", filePath);
                player.sendMessage("Video loading failed or stopped: " + args[0]);
            }
        });

        return true;
    }

    private boolean stopVideoCommand(Player player, String[] args) {
        if (!playerHasScreen(player)) {
            player.sendMessage("No screen is currently selected!");
            return true;
        }

        ScreenObject screen = getPlayerScreen(player);
        if (getScreenWorker(screen) == null || getScreenWorker(screen).isState(PlaybackWorker.State.IDLE)) {
            player.sendMessage("Nothing is playing on this screen!");
            return true;
        }

        CommandScreen.removeScreenWorker(screen).stop();
        player.sendMessage("Stopped playing on screen.");
        return true;
    }

    private boolean pauseVideoCommand(Player player, String[] args) {
        if (!playerHasScreen(player)) {
            player.sendMessage("No screen is currently selected!");
            return true;
        }

        ScreenObject screen = getPlayerScreen(player);
        PlaybackWorker worker = getScreenWorker(screen);
        if (worker == null || worker.isState(PlaybackWorker.State.IDLE)) {
            player.sendMessage("Nothing is playing on this screen!");
            return true;
        }

        if (worker.isState(PlaybackWorker.State.PAUSED)) {
            worker.unpause();
            player.sendMessage("Resumed screen playback.");
        } else {
            worker.pause();
            player.sendMessage("Paused screen playback.");
        }

        return true;
    }

    private static boolean playerHasScreen(Player player) {
        return selectedScreens.containsKey(player.getUniqueId());
    }

    private static void setPlayerScreen(Player player, ScreenObject screen) {
        selectedScreens.put(player.getUniqueId(), screen);
    }

    private static ScreenObject getPlayerScreen(Player player) {
        return selectedScreens.get(player.getUniqueId());
    }

    private static ScreenObject removePlayerScreen(Player player) {
        return selectedScreens.remove(player.getUniqueId());
    }

    private static boolean screenHasWorker(ScreenObject screen) {
        return currentWorkers.containsKey(screen);
    }

    private static void setScreenWorker(ScreenObject screen, PlaybackWorker worker) {
        currentWorkers.put(screen, worker);
    }

    private static PlaybackWorker getScreenWorker(ScreenObject screen) {
        return currentWorkers.get(screen);
    }

    private static PlaybackWorker removeScreenWorker(ScreenObject screen) {
        return currentWorkers.remove(screen);
    }
}
