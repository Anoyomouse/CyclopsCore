package org.cyclops.cyclopscore.config.configurable;

import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.BlockPropertyManagerComponent;
import org.cyclops.cyclopscore.block.IBlockPropertyManager;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TileEntityNBTStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Block with a tile entity that can hold ExtendedConfigs.
 * @author rubensworks
 *
 */
public class ConfigurableBlockContainer extends BlockContainer implements IConfigurable {

    @Delegate private IBlockPropertyManager propertyManager;
    @Override protected BlockState createBlockState() {
        return (propertyManager = new BlockPropertyManagerComponent(this)).createDelegatedBlockState();
    }

    @SuppressWarnings("rawtypes")
    protected ExtendedConfig eConfig = null;
    
    protected Random random;
    private Class<? extends CyclopsTileEntity> tileEntity;
    
    protected boolean hasGui = false;
    
    private boolean rotatable;
    
    protected int pass = 0;
    protected boolean isInventoryBlock = false;
    
    /**
     * Make a new blockState instance.
     * @param eConfig Config for this blockState.
     * @param material Material of this blockState.
     * @param tileEntity The class of the tile entity this blockState holds.
     */
    @SuppressWarnings({ "rawtypes" })
    public ConfigurableBlockContainer(ExtendedConfig eConfig, Material material, Class<? extends CyclopsTileEntity> tileEntity) {
        super(material);
        this.setConfig(eConfig);
        this.setUnlocalizedName(eConfig.getUnlocalizedName());
        this.random = new Random();
        this.tileEntity = tileEntity;
        setHardness(5F);
        setStepSound(Block.soundTypePiston);
    }
    
    /**
     * Get the class of the tile entity this blockState holds.
     * @return The tile entity class.
     */
    public Class<? extends TileEntity> getTileEntity() {
        return this.tileEntity;
    }

    @SuppressWarnings("rawtypes")
    private void setConfig(ExtendedConfig eConfig) {
        this.eConfig = eConfig;
    }
    
    /**
     * If this blockState container has a corresponding GUI.
     * @return If it has a GUI.
     */
    public boolean hasGui() {
        return hasGui;
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        try {
            CyclopsTileEntity tile = tileEntity.newInstance();
            tile.onLoad();
            tile.setRotatable(isRotatable());
            return tile;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    /**
     * If the NBT data of this tile entity should be added to the dropped blockState.
     * @return If the NBT data should be added.
     */
    public boolean saveNBTToDroppedItem() {
        return true;
    }
    
    protected void onPreBlockDestroyed(World world, BlockPos blockPos) {
    	MinecraftHelpers.preDestroyBlock(this, world, blockPos, saveNBTToDroppedItem());
    }
    
    protected void onPostBlockDestroyed(World world, BlockPos blockPos) {
    	
    }
    
    @Override
    public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
    	onPreBlockDestroyed(world, blockPos);
        super.breakBlock(world, blockPos, blockState);
        onPostBlockDestroyed(world, blockPos);
    }
    
    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos blockPos, Explosion explosion) {
    	onPreBlockDestroyed(world, blockPos);
    	super.onBlockDestroyedByExplosion(world, blockPos, explosion);
    	onPostBlockDestroyed(world, blockPos);
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entity, ItemStack stack) {
        if(entity != null) {
            CyclopsTileEntity tile = (CyclopsTileEntity) world.getTileEntity(blockPos);
            
            if(stack.getTagCompound() != null) {
                    stack.getTagCompound().setInteger("x", blockPos.getX());
                    stack.getTagCompound().setInteger("y", blockPos.getY());
                    stack.getTagCompound().setInteger("z", blockPos.getZ());
                    //stack.getTagCompound().setInteger("rotation", UNKNOWN.ordinal());
                    tile.readFromNBT(stack.getTagCompound());
            }
            
            /*if(tile.isRotatable()) {
                EnumFacing facing = DirectionHelpers.getEntityFacingDirection(entity);
                tile.setRotation(facing);
            }*/

            if(tile instanceof IUpdatePlayerListBox) {
                ((IUpdatePlayerListBox) tile).update();
            }
        }
        super.onBlockPlacedBy(world, blockPos, blockState, entity, stack);
    }
    
    /**
     * Write additional info about the tile into the item.
     * @param tile The tile that is being broken.
     * @param tag The tag that will be added to the dropped item.
     */
    public void writeAdditionalInfo(TileEntity tile, NBTTagCompound tag) {
    	
    }
    
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos blockPos, IBlockState blockState, int fortune) {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        ItemStack itemStack = new ItemStack(getItemDropped(blockState, new Random(), fortune), 1, damageDropped(blockState));
		if(TileEntityNBTStorage.TAG != null) {
		    itemStack.setTagCompound(TileEntityNBTStorage.TAG);
		}
		drops.add(itemStack);
        
        MinecraftHelpers.postDestroyBlock(world, blockPos);
        return drops;
    }

    /**
     * If the NBT data of this blockState should be preserved in the item when it
     * is broken into an item.
     * @return If it should keep NBT data.
     */
    public boolean isKeepNBTOnDrop() {
		return true;
	}

	/**
     * If this blockState can be rotated.
     * @return Can be rotated.
     */
    public boolean isRotatable() {
        return rotatable;
    }

    /**
     * Set whether of not this container must be able to be rotated.
     * @param rotatable Can be rotated.
     */
    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }
    
    /**
     * Get the texture path of the GUI.
     * @return The path of the GUI for this blockState.
     */
    public String getGuiTexture() {
        return getGuiTexture("");
    }
    
    /**
     * Get the texture path of the GUI.
     * @param suffix Suffix to add to the path.
     * @return The path of the GUI for this blockState.
     */
    public String getGuiTexture(String suffix) {
        return getConfig().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + eConfig.getNamedId() + "_gui" + suffix + ".png";
    }
    
    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos blockPos) {
    	Item item = getItem(world, blockPos);

        if (item == null) {
            return null;
        }

        ItemStack itemStack = new ItemStack(item, 1, getDamageValue(world, blockPos));
        TileEntity tile = world.getTileEntity(blockPos);
        if (tile instanceof CyclopsTileEntity && isKeepNBTOnDrop()) {
            CyclopsTileEntity ecTile = ((CyclopsTileEntity) tile);
            itemStack.setTagCompound(ecTile.getNBTTagCompound());
        }
        return itemStack;
    }

    @Override
    public final BlockContainerConfig getConfig() {
        return (BlockContainerConfig) this.eConfig;
    }

}
