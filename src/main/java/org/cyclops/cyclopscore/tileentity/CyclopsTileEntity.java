package org.cyclops.cyclopscore.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * A base class for all the tile entities.
 * This tile does not tick, use the TickingEvilCraftTileEntity variant for that.
 * @author rubensworks
 *
 */
public class CyclopsTileEntity extends TileEntity {
    
    private List<Field> nbtPersistedFields = null;
    
    @NBTPersist
    private Boolean rotatable = false;
    private EnumFacing rotation = EnumFacing.NORTH;
    
    /**
     * Make a new instance.
     */
    public CyclopsTileEntity() {
        generateNBTPersistedFields();
    }
    
    /**
     * Called when the blockState of this tile entity is destroyed.
     */
    public void destroy() {
        invalidate();
    }
    
    /**
     * If this entity is interactable with a player.
     * @param entityPlayer The player that is checked.
     * @return If the given player can interact.
     */
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return true;
    }
    
    private void generateNBTPersistedFields() {
        nbtPersistedFields = new LinkedList<Field>();
        for(Class<?> clazz = this.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
	        for(Field field : clazz.getDeclaredFields()) {
	            if(field.isAnnotationPresent(NBTPersist.class)) {         
	                nbtPersistedFields.add(field);
	            }
	        }
        }
    }
    
    private void writePersistedField(Field field, NBTTagCompound tag) {
        NBTClassType.performActionForField(this, field, tag, true);
    }
    
    private void readPersistedField(Field field, NBTTagCompound tag) {
        NBTClassType.performActionForField(this, field, tag, false);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for(Field field : nbtPersistedFields)
            writePersistedField(field, tag);
        
        // Separate action for direction
        tag.setString("rotation", rotation.getName());
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for(Field field : nbtPersistedFields)
            readPersistedField(field, tag);
        
        // Separate action for direction
        EnumFacing foundRotation = EnumFacing.byName(tag.getString("rotation"));
        if(foundRotation != null) {
        	rotation = foundRotation;
        }
        onLoad();
    }

    /**
     * When the tile is loaded or created.
     */
    public void onLoad() {

    }
    
    /**
     * Get the NBT tag for this tile entity.
     * @return The NBT tag that is created with the
     * {@link CyclopsTileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)} method.
     */
    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return tag;
    }

    /**
     * If the blockState this tile entity has can be rotated.
     * @return If it can be rotated.
     */
    public boolean isRotatable() {
        return this.rotatable;
    }

    /**
     * Set whether or not the blockState that has this tile entity can be rotated.
     * @param rotatable If it can be rotated.
     */
    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    /**
     * Get the current rotation of this tile entity.
     * Default is {@link net.minecraft.util.EnumFacing#NORTH}.
     * @return The rotation.
     */
    public EnumFacing getRotation() {
        return rotation;
    }

    /**
     * Set the rotation of this tile entity.
     * Default is {@link net.minecraft.util.EnumFacing#NORTH}.
     * @param rotation The new rotation.
     */
    public void setRotation(EnumFacing rotation) {
        this.rotation = rotation;
    }
    
    /**
     * Get the blockState type this tile entity is defined for.
     * @return The blockState instance.
     */
    public ConfigurableBlockContainer getBlock() {
        return (ConfigurableBlockContainer) this.getBlockType();
    }

}
