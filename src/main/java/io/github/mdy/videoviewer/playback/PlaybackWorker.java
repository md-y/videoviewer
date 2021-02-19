package io.github.mdy.videoviewer.playback;

import io.github.mdy.videoviewer.VideoViewer;
import io.github.mdy.videoviewer.screen.ScreenObject;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.awt.image.BufferedImage;
import java.io.File;

public class PlaybackWorker implements Runnable {
    private final ScreenObject screen;
    private final int frameHeight, frameWidth;
    private BufferedImage[] frames;
    private BukkitTask task;
    private int currentFrame;
    private String fileName = "Unknown";

    public enum State {
        IDLE,
        LOADING,
        PLAYING,
        PAUSED,
        STOPPED
    }
    private State currentState;

    public PlaybackWorker(ScreenObject screen) {
        this.screen = screen;
        this.frameWidth = screen.getWidth();
        this.frameHeight = screen.getHeight();
    }

    public boolean loadVideo(String filePath) {
        this.currentState = State.LOADING;
        File file = new File(filePath);
        if (!file.exists()) return false;
        this.fileName = file.getName();

        BufferedImage[] processedFrames = VideoDecoder.getImageArraySync(file, this.frameWidth, this.frameHeight);

        if (this.isState(State.STOPPED)) return true;
        else this.currentState = State.IDLE;

        if (processedFrames == null) return false;
        this.frames = processedFrames;
        return true;
    }

    public void start() {
        if (this.frames == null) {
            VideoViewer.log("Tried to play video without any frames loaded. Ignored.");
            return;
        }

        this.currentFrame = 0;
        this.task = Bukkit.getScheduler().runTaskTimer(VideoViewer.instance, this, 1, 1);
        this.currentState = State.PLAYING;
    }

    public void stop() {
        this.frames = null;
        this.currentState = State.STOPPED;
        if (this.task == null || this.task.isCancelled()) return;
        this.task.cancel();
        this.task = null;
    }

    @Override
    public void run() {
        if (this.isState(State.PLAYING)) {
            if (this.currentFrame < this.frames.length) {
                BufferedImage img = this.frames[this.currentFrame];
                if (img != null) this.screen.displayImage(img);
                this.currentFrame++;
            } else {
                this.stop();
            }
        }
    }

    public void pause() {
        if (this.isState(State.PLAYING)) this.currentState = State.PAUSED;
    }

    public void unpause() {
        if (this.isState(State.PAUSED)) this.currentState = State.PLAYING;
    }

    public String getFileName() {
        return this.fileName;
    }

    public boolean isState(State state) {
        return this.currentState == state;
    }
}
