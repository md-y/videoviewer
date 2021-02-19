package io.github.mdy.videoviewer.screen;

import io.github.mdy.videoviewer.VideoViewer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.awt.image.BufferedImage;
import java.util.*;

public class ScreenObject {
    private final UUID id;
    private final int width, height;
    private final PixelEntity[] pixelEntities;

    // New ScreenObject
    public ScreenObject(int width, int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Negative Dimension");

        this.id = UUID.fromString(UUID.randomUUID().toString()); // Needed to make only 1 uuid
        this.width = width;
        this.height = height;
        this.pixelEntities = new PixelEntity[width * height];
    }

    // From existing PixelEntities
    public ScreenObject(PixelEntity[] pixelEntities) {
        if (pixelEntities.length == 0) throw new IllegalArgumentException("Invalid PixelEntity Array");
        PixelEntity root = pixelEntities[0];

        this.id = root.getScreenId();
        this.width = root.getScreenWidth();
        this.height = root.getScreenHeight();

        if (pixelEntities.length != this.width * this.height)
            throw new IllegalArgumentException("Invalid number of elements in PixelEntity array");
        for (PixelEntity entity : pixelEntities) if (entity == null)
            throw new IllegalArgumentException("PixelEntity array contains null elements");
        this.pixelEntities = pixelEntities;
    }

    public void build(Location location, EntityType type) {
        int yaw = Math.round(location.getYaw() / 90) * 90;
        if (yaw < 0) yaw += 360;
        Vector directionVector = new Vector(
                (yaw == 270 ? 1 : yaw == 90 ? -1 : 0),
                1,
                (yaw == 0 || yaw == 360 ? 1 : yaw == 180 ? -1 : 0)
        );

        for (int i = 0; i < pixelEntities.length; i++) {
            PixelEntity ent = getPixelEntityFromType(type, this, i);
            if (ent == null) throw new IllegalArgumentException("Invalid entity type.");

            Location newLocation = location.clone();
            Vector offsetVector = new Vector(i % this.width, i / this.width, i % this.width);
            offsetVector = offsetVector.multiply(directionVector).multiply(ent.getEntityWidth());
            newLocation.add(offsetVector);
            newLocation.setYaw(yaw + 90f); // Screen faces to the right of its axis

            ent.spawn(newLocation);
            pixelEntities[i] = ent;
        }
    }

    private static PixelEntity getPixelEntityFromType(EntityType type, Entity entity) {
        switch (type) {
            case SHEEP:
                return new SheepPixelEntity(entity);
            case ARMOR_STAND:
                return new ArmorPixelEntity(entity);
            default:
                return null;
        }
    }

    private static PixelEntity getPixelEntityFromType(EntityType type, ScreenObject screen, int index) {
        switch (type) {
            case SHEEP:
                return new SheepPixelEntity(screen, index);
            case ARMOR_STAND:
                return new ArmorPixelEntity(screen, index);
            default:
                return null;
        }
    }

    public static ScreenObject getScreenFromEntity(Entity baseEntity) {
        NamespacedKey idKey = new NamespacedKey(VideoViewer.instance, "screenId");
        NamespacedKey metaKey = new NamespacedKey(VideoViewer.instance, "screenMeta");

        PersistentDataContainer basePDC = baseEntity.getPersistentDataContainer();
        if (basePDC.has(idKey, PersistentDataType.STRING)) {
            // Read information from the first entity
            String id = basePDC.get(idKey, PersistentDataType.STRING);
            int[] metaArr = basePDC.get(metaKey, PersistentDataType.INTEGER_ARRAY);
            if (id == null || metaArr == null) return null;

            // Find all other screen entities
            int width = metaArr[1];
            int height = metaArr[2];
            List<Entity> allEntities = baseEntity.getNearbyEntities(width*4, metaArr[2]*4 , width*4);
            allEntities.add(baseEntity);
            PixelEntity[] foundEntities = new PixelEntity[width * height];

            for (Entity entity : allEntities) {
                PersistentDataContainer pdc = entity.getPersistentDataContainer();
                if (entity.getType() != baseEntity.getType() || !pdc.has(idKey, PersistentDataType.STRING)) continue;
                String newId = pdc.get(idKey, PersistentDataType.STRING);
                metaArr = pdc.get(metaKey, PersistentDataType.INTEGER_ARRAY);
                if (newId == null || metaArr == null || !newId.equals(id)) continue;

                foundEntities[metaArr[0]] = getPixelEntityFromType(entity.getType(), entity);
            }

            return new ScreenObject(foundEntities);
        }

        return null;
    }

    public static ScreenObject findClosestLoadedScreen(Location location) {
        NamespacedKey idKey = new NamespacedKey(VideoViewer.instance, "screenId");

        if (location.getWorld() == null) return null;
        Collection<Entity> entities = location.getWorld().getEntities();
        Entity bestEntity = null;
        double bestDistance = -1;
        for (Entity entity : entities) {
            if (entity.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
                double entityDistance = entity.getLocation().distanceSquared(location);
                if (bestDistance < 0 || entityDistance < bestDistance) {
                    bestDistance = entityDistance;
                    bestEntity = entity;
                }
            }
        }

        if (bestEntity == null) return null;
        else return getScreenFromEntity(bestEntity);
    }

    public void displayImage(BufferedImage image) {
        if (image == null)
            throw new IllegalArgumentException("Tried to display null image.");
        if (image.getWidth() != this.width || image.getHeight() != this.height)
            throw new IllegalArgumentException("Image is wrong resolution to display.");

        for (int i = 0; i < this.pixelEntities.length; i++) {
            // The image must be flipped upside down
            java.awt.Color javaColor = new java.awt.Color(image.getRGB(i % this.width, this.height - i / this.width - 1));
            Color color = Color.fromRGB(javaColor.getRed(), javaColor.getGreen(), javaColor.getBlue());
            this.pixelEntities[i].setColor(color);
        }
    }

    public void delete() {
        for (PixelEntity entity : pixelEntities) entity.delete();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object screen) {
        if (screen instanceof ScreenObject) return this.getId().equals(((ScreenObject)screen).getId());
        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
