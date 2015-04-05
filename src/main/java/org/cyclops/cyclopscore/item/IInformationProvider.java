package org.cyclops.cyclopscore.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/**
 * Interface that can be applied to {@link net.minecraft.block.Block} or {@link net.minecraft.item.Item} so that they can provide information
 * when the player hovers over the item in their inventory.
 * @see InformationProviderComponent
 * @author rubensworks
 *
 */
public interface IInformationProvider {
    
    /**
     * A prefix for blockState information.
     */
    public static String BLOCK_PREFIX = EnumChatFormatting.RED.toString();
    /**
     * A prefix for item information.
     */
    public static String ITEM_PREFIX = BLOCK_PREFIX;
    /**
     * A prefix for additional info.
     */
    public static String INFO_PREFIX = EnumChatFormatting.DARK_PURPLE.toString() + EnumChatFormatting.ITALIC.toString();
    
    /**
     * Get info for a given itemStack.
     * @param itemStack The itemStack that must be given information.
     * @return Information for that itemStack.
     */
    public String getInfo(ItemStack itemStack);
    /**
     * An extended way to provide additional information.
     * @param itemStack The itemStack that must be given information. 
     * @param entityPlayer The player that asks for information.
     * @param list The list of information.
     * @param par4 No idea...
     */
    public void provideInformation(ItemStack itemStack, EntityPlayer entityPlayer, @SuppressWarnings("rawtypes") List list, boolean par4);
}
