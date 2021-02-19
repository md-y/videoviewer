package io.github.mdy.videoviewer.screen;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;

public class SheepPixelEntity extends PixelEntity {
    private Sheep entity;

    public SheepPixelEntity(ScreenObject screen, int id) {
        super(screen, id);
    }

    SheepPixelEntity(Entity registerEntity) {
        super(registerEntity);

        if (registerEntity instanceof Sheep) this.entity = (Sheep) registerEntity;
        else throw new IllegalArgumentException("This PixelEntity expects a Sheep.");
    }

    @Override
    void setColor(Color color) {
        DyeColor dyeColor = getDyeColor(color);
        this.entity.setColor(dyeColor);
    }

    @Override
    void spawn(Location location) {
        location.setPitch(-90f);
        this.entity = (Sheep) location.getWorld().spawnEntity(location, EntityType.SHEEP);
        this.entity.setGravity(false);
        this.entity.setAI(false);
        this.entity.setSilent(true);
        this.entity.setBaby();
        this.entity.setInvulnerable(true); // Does not apply to creative players
        this.entity.setAgeLock(true);
        this.entity.setCollidable(false);
        this.entity.setPersistent(true);

        this.writePersistentDataToEntity(this.entity);
    }

    @Override
    void delete() {
        this.entity.remove();
    }

    @Override
    public Double getEntityWidth() {
        return 0.45;
    }

    private static DyeColor getDyeColor(Color color) {
        DyeColor[] colors = DyeColor.values();
        DyeColor bestColor = DyeColor.WHITE;
        float bestDifference = -1;
        for (DyeColor dyeColor : colors) {
            Color dyeColorValue = dyeColor.getColor();
            float difference = dyeColorValue.getRed() + dyeColorValue.getGreen() + dyeColorValue.getBlue();
            difference -= color.getRed() + color.getGreen() + color.getBlue();
            difference = Math.abs(difference);
            if (bestDifference == -1 || difference < bestDifference) {
                bestColor = dyeColor;
                bestDifference = difference;
            }
        }

        return bestColor;
    }

}
