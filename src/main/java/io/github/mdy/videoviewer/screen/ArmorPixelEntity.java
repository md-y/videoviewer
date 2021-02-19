package io.github.mdy.videoviewer.screen;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ArmorPixelEntity extends PixelEntity {
    private ArmorStand entity;

    public ArmorPixelEntity(ScreenObject screen, int id) {
        super(screen, id);
    }

    ArmorPixelEntity(Entity registerEntity) {
        super(registerEntity);

        if (registerEntity instanceof ArmorStand) this.entity = (ArmorStand) registerEntity;
        else throw new IllegalArgumentException("This PixelEntity expects a ArmorStand.");
    }

    @Override
    void setColor(Color color) {
        ItemStack armor = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        meta.setColor(color);
        armor.setItemMeta(meta);
        this.entity.getEquipment().setChestplate(armor);
    }

    @Override
    void spawn(Location location) {
        this.entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        this.entity.setGravity(false);
        this.entity.setAI(false);
        this.entity.setSilent(true);
        this.entity.setSmall(true);
        this.entity.setInvulnerable(true); // Does not apply to creative players
        this.entity.setCollidable(false);
        this.entity.setPersistent(true);
        this.entity.setBasePlate(false);
        this.entity.setInvisible(true);

        this.entity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        this.writePersistentDataToEntity(this.entity);
    }

    @Override
    void delete() {
        this.entity.remove();
    }

    @Override
    Double getEntityWidth() {
        return 0.32;
    }
}
