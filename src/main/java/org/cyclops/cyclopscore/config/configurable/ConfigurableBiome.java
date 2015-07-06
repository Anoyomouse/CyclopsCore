package org.cyclops.cyclopscore.config.configurable;

import net.minecraft.world.biome.BiomeGenBase;
import org.cyclops.cyclopscore.config.extendedconfig.BiomeConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;

/**
 * A simple configurable for Biomes, will auto-register itself after construction.
 * @author rubensworks
 *
 */
public class ConfigurableBiome extends BiomeGenBase implements IConfigurable {

    protected BiomeConfig eConfig = null;
    
    /**
     * Make a new Biome instance
     * @param eConfig Config for this enchantment.
     */
    protected ConfigurableBiome(BiomeConfig eConfig) {
        super(eConfig.getId());
        this.setConfig(eConfig);
        this.setBiomeName(getLocalizedName());
        
    }
    
    @SuppressWarnings("rawtypes")
    private void setConfig(ExtendedConfig eConfig) {
        this.eConfig = (BiomeConfig)eConfig;
    }
    
    /**
     * Get localized name of this biome.
     * @return Localized name.
     */
    public String getLocalizedName() {
        return L10NHelpers.localize(eConfig.getUnlocalizedName());
    }

    @Override
    public ExtendedConfig<?> getConfig() {
        return eConfig;
    }

}
