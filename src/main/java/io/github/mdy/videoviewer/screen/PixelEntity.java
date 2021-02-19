package io.github.mdy.videoviewer.screen;

import io.github.mdy.videoviewer.VideoViewer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

abstract class PixelEntity {
    private final UUID screenId;
    private final int id, screenWidth, screenHeight;

    protected static final NamespacedKey screenKey = new NamespacedKey(VideoViewer.instance, "screenId");
    protected static final NamespacedKey metaKey = new NamespacedKey(VideoViewer.instance, "screenMeta");

    // Generate new Entity
    public PixelEntity(ScreenObject screen, int id) {
        this.id = id;
        this.screenId = screen.getId();
        this.screenWidth = screen.getWidth();
        this.screenHeight = screen.getHeight();
    }

    // Generate from existing Entity
    PixelEntity(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(screenKey, PersistentDataType.STRING) || !pdc.has(metaKey, PersistentDataType.INTEGER_ARRAY)) {
            throw new IllegalArgumentException(
                    "Entity to parse into a PixelEntity does not have the needed persistent data."
            );
        }

        String screenId = pdc.get(screenKey, PersistentDataType.STRING);
        int[] metaArr = pdc.get(metaKey, PersistentDataType.INTEGER_ARRAY);
        if (screenId == null || metaArr == null) throw new IllegalArgumentException("Corrupted PersistentData.");

        this.screenId = UUID.fromString(screenId);
        this.id = metaArr[0];
        this.screenWidth = metaArr[1];
        this.screenHeight = metaArr[2];
    }

    abstract void setColor(Color color);

    abstract void spawn(Location location);

    abstract void delete();

    abstract Double getEntityWidth();

    protected final void writePersistentDataToEntity(Entity entity) {
        entity.getPersistentDataContainer().set(screenKey, PersistentDataType.STRING, this.screenId.toString());
        int[] metaArr = { this.id, this.screenWidth, this.screenHeight };
        entity.getPersistentDataContainer().set(metaKey, PersistentDataType.INTEGER_ARRAY, metaArr);
    }

    public UUID getScreenId() {
        return screenId;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getId() {
        return id;
    }
}
