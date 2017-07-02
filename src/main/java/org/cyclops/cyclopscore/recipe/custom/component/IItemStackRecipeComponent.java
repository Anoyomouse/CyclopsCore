package org.cyclops.cyclopscore.recipe.custom.component;

import net.minecraft.item.ItemStack;

/**
 * Interface for recipe components that hold an {@link net.minecraft.item.ItemStack}.
 * @author immortaleeb
 */
public interface IItemStackRecipeComponent {
    /**
     * @return Returns the ItemStack held by this recipe component.
     */
    public ItemStack getIngredient();
}
