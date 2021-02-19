package io.github.mdy.videoviewer.playback;

import io.github.mdy.videoviewer.VideoViewer;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public abstract class VideoDecoder {
    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "video_viewer_cache");
    static {
        avutil.av_log_set_level(avutil.AV_LOG_QUIET); // Mute FFmpeg
    }

    public static BufferedImage[] getImageArraySync(File originalFile, int width, int height) {
        return getImageArraySync(originalFile, width, height, false);
    }

    public static BufferedImage[] getImageArraySync(File originalFile, int width, int height, boolean clearCache) {
        File cachedFile = getCachedFramesFile(originalFile, width, height);
        File[] imageFiles = cachedFile.listFiles();
        if (clearCache && imageFiles != null) {
            VideoViewer.log("Clearing cache", cachedFile.getName());
            for (File file : imageFiles) file.delete();
            cachedFile.delete();
        }
        if (clearCache || !cachedFile.exists()) createCacheFile(originalFile, width, height);

        try {
            // Refresh file list
            if ((imageFiles = cachedFile.listFiles()) != null) {
                // Read frame image files
                BufferedImage[] frames = new BufferedImage[imageFiles.length];
                for (File imageFile : imageFiles) {
                    int frameIndex = Integer.parseInt(imageFile.getName().replace(".png", ""));
                    if (frameIndex < frames.length) frames[frameIndex] = ImageIO.read(imageFile);
                }

                // Verify frames
                int missingFrames = 0;
                for (BufferedImage frame : frames) if (frame == null) missingFrames++;
                if (missingFrames > 0) return frames;
                else VideoViewer.log("Missing", missingFrames, "frame files.");
            }
        } catch (IOException e) {
            VideoViewer.log("Invalid frame file data.");
            e.printStackTrace();
        } catch (NumberFormatException | NullPointerException ignored) {
            VideoViewer.log("Invalid frame file name.");
        }

        // Attempt to clear cache
        if (!clearCache) return getImageArraySync(originalFile, width, height, true);
        else return null;
    }

    private static void createCacheFile(File originalFile, int width, int height) {
        VideoViewer.log("Creating cache file for", originalFile.getAbsolutePath());

        try {
            File cachedFile = getCachedFramesFile(originalFile, width, height);
            if (cachedFile.exists()) cachedFile.delete();
            else cachedFile.mkdirs();

            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(originalFile);
            grabber.start();

            String ffmpegPath = Paths.get(cachedFile.getAbsolutePath(), "%d.png").toString();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegPath, width, height);
            recorder.start();

            FFmpegFrameFilter filter = new FFmpegFrameFilter(
                    "fps=fps=20,scale=" + width + ":" + height,
                    grabber.getImageWidth(),
                    grabber.getImageHeight()
            );
            filter.setPixelFormat(grabber.getPixelFormat());
            filter.start();

            Frame frameGrab, framePull;
            while ((frameGrab = grabber.grabFrame(false, true, true, false)) != null) {
                if (frameGrab.image == null && frameGrab.samples == null) continue;
                filter.push(frameGrab);
                framePull = filter.pull();
                if (framePull == null || framePull.image == null && framePull.samples == null) continue;
                recorder.record(framePull);
            }

            grabber.stop();
            grabber.release();
            recorder.stop();
            recorder.release();
            filter.stop();
            filter.release();
        } catch (FrameGrabber.Exception | FrameRecorder.Exception | FrameFilter.Exception e) {
            VideoViewer.log("Failed to export cache frames for", originalFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static String getCacheName(File file, int width, int height) {
        return UUID.nameUUIDFromBytes(file.getAbsolutePath().getBytes()) + "_" + width + "_" + height;
    }

    public static File getCachedFramesFile(File file, int width, int height) {
        return new File(TMP_DIR, getCacheName(file, width, height));
    }

    public static void clearCache(String id) {
        File folder = new File(TMP_DIR, id);
        if (!folder.exists()) return;
        File[] images = folder.listFiles();
        if (images != null) for (File image : images) image.delete();
        folder.delete();
    }

    public static void clearCache() {
        File[] cacheFolders = TMP_DIR.listFiles();
        if (cacheFolders == null) return;
        for (File folder : cacheFolders) clearCache(folder.getName());
        TMP_DIR.delete();
    }
}