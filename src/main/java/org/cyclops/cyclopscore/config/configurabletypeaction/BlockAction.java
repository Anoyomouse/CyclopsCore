package org.cyclops.cyclopscore.config.configurabletypeaction;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.config.ConfigurableType;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.configurable.IConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The action used for {@link BlockConfig}.
 * @author rubensworks
 * @see ConfigurableTypeAction
 */
public class BlockAction extends ConfigurableTypeAction<BlockConfig> {

    private static final List<BlockConfig> MODEL_ENTRIES = Lists.newArrayList();

    static {
        MinecraftForge.EVENT_BUS.register(BlockAction.class);
    }

    /**
     * Registers a block.
     * @param block The block instance.
     * @param config The config.
     * @param creativeTabs The creative tab this block will reside in.
     */
    public static void register(Block block, ExtendedConfig config, @Nullable CreativeTabs creativeTabs) {
        register(block, null, config, creativeTabs);
    }

    /**
     * Registers a block.
     * @param block The block instance.
     * @param itemBlockClass The optional item block class.
     * @param config The config.
     * @param creativeTabs The creative tab this block will reside in.
     */
    public static void register(Block block, @Nullable Class<? extends ItemBlock> itemBlockClass, ExtendedConfig config, @Nullable CreativeTabs creativeTabs) {
        register(block, config);
        if(itemBlockClass != null) {
            try {
                ItemBlock item = itemBlockClass.getConstructor(Block.class).newInstance(block);
                register(ForgeRegistries.ITEMS, item, config);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(creativeTabs != null) {
            block.setCreativeTab(creativeTabs);
        }
    }

    @Override
    public void preRun(BlockConfig eConfig, Configuration config, boolean startup) {
        // Get property in config file and set comment
        Property property = config.get(eConfig.getHolderType().getCategory(), eConfig.getNamedId(),
        		eConfig.isEnabled());
        property.setRequiresMcRestart(true);
        property.setComment(eConfig.getComment());
        property.setLanguageKey(eConfig.getFullUnlocalizedName());
        
        if(startup) {
	        // Update the ID, it could've changed
	        eConfig.setEnabled(property.getBoolean(true));
        }
    }

    @Override
    public void postRun(BlockConfig eConfig, Configuration config) {
        // Save the config inside the correct element
        eConfig.save();

        Block block = (Block) eConfig.getSubInstance();

        // Register block and set creative tab.
        register(block, eConfig.getItemBlockClass(), eConfig, eConfig.getTargetTab());

        // Also register tile entity
        GuiHandler.GuiType guiType = GuiHandler.GuiType.BLOCK;
        if(eConfig.getHolderType().equals(ConfigurableType.BLOCKCONTAINER)) {
            ConfigurableBlockContainer container = (ConfigurableBlockContainer) block;
            // This alternative registration is required to remain compatible with old worlds.
            try {
                GameRegistry.registerTileEntity(container.getTileEntity(),
                        eConfig.getMod().getModId() + ":" + eConfig.getSubUniqueName());
            } catch (IllegalArgumentException e) {
                // Ignore duplicate tile entity registration errors
            }
            guiType = GuiHandler.GuiType.TILE;
        }

        // If the block has a GUI, go ahead and register that.
        if(block instanceof IConfigurableBlock && ((IConfigurableBlock) block).hasGui()) {
            IGuiContainerProvider gui = (IGuiContainerProvider) block;
            eConfig.getMod().getGuiHandler().registerGUI(gui, guiType);
        }
        
        // Register optional ore dictionary ID
        if(eConfig.getOreDictionaryId() != null) {
            OreDictionary.registerOre(eConfig.getOreDictionaryId(), new ItemStack((Block)eConfig.getSubInstance()));
        }
        
        // Register third-party mod blockState parts.
        // TODO: enable when FMP is updated.
        /*if(eConfig.isMultipartEnabled()) {
            ForgeMultipartHelper.registerMicroblock(eConfig);
        }*/

        if (MinecraftHelpers.isClientSide()) {
            ItemAction.handleItemModel(eConfig);
            if(block instanceof IConfigurableBlock) {
                IConfigurableBlock configurableBlock = (IConfigurableBlock) block;
                IBlockColor blockColorHandler = configurableBlock.getBlockColorHandler();
                if (blockColorHandler != null) {
                    Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(blockColorHandler, block);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistryLoad(ModelRegistryEvent event) {
        for (BlockConfig config : MODEL_ENTRIES) {
            Pair<ModelResourceLocation, ModelResourceLocation> resourceLocations = config.registerDynamicModel();
            config.dynamicBlockVariantLocation = resourceLocations.getLeft();
            config.dynamicItemVariantLocation  = resourceLocations.getRight();
        }

    }

    public static void handleDynamicBlockModel(BlockConfig extendedConfig) {
        MODEL_ENTRIES.add(extendedConfig);
    }

}
