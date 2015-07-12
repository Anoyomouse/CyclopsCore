package org.cyclops.cyclopscore.config.extendedconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.ConfigurableType;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * Config for entities.
 * For mobs, there is the {@link MobConfig}.
 * For entities with custom models there is {@link ModelEntityConfig}.
 * @author rubensworks
 * @see ExtendedConfig
 */
public abstract class EntityConfig extends ExtendedConfig<EntityConfig>{

    /**
     * Make a new instance.
     * @param mod     The mod instance.
     * @param enabled If this should is enabled.
     * @param namedId The unique name ID for the configurable.
     * @param comment The comment to add in the config file for this configurable.
     * @param element The class of this configurable.
     */
    public EntityConfig(ModBase mod, boolean enabled, String namedId, String comment, Class<? extends Entity> element) {
        super(mod, enabled, namedId, comment, element);
    }
    
    @Override
	public String getUnlocalizedName() {
		return "entity." + getNamedId();
	}
    
    @Override
	public ConfigurableType getHolderType() {
		return ConfigurableType.ENTITY;
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void onInit(Step step) {
        super.onInit(step);
        if(step == Step.INIT) {
            Render render = getRender(Minecraft.getMinecraft().getRenderManager(),
                    Minecraft.getMinecraft().getRenderItem());
            if(render != null) {
                @SuppressWarnings("unchecked")
                Class<? extends Entity> clazz = (Class<? extends Entity>) this.getElement();
                getMod().getProxy().registerRenderer(clazz, render);
            }
        }
    }
    
    /**
     * The range at which MC will send tracking updates.
     * @return The tracking range.
     */
    public int getTrackingRange() {
        return 160;
    }
    
    /**
     * The frequency of tracking updates.
     * @return The update frequency.
     */
    public int getUpdateFrequency() {
        return 10;
    }
    
    /**
     * Whether to send velocity information packets as well.
     * @return Send velocity updates?
     */
    public boolean sendVelocityUpdates() {
        return false;
    }
    
    protected abstract Render getRender(RenderManager renderManager, RenderItem renderItem);
}
