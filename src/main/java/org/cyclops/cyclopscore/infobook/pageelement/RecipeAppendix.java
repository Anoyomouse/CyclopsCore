package org.cyclops.cyclopscore.infobook.pageelement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.infobook.*;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

/**
 * Recipes that can be added to sections.
 * @author rubensworks
 */
public abstract class RecipeAppendix<T> extends SectionAppendix {

    protected static final int SLOT_SIZE = 16;
    protected static final int TICK_DELAY = 30;

    protected T recipe;

    /**
     * This map holds advanced buttons that have a unique identifier.
     * The map has to be populated in the baking of this appendix.
     * The map values can be updated on each render tick.
     */
    protected Map<AdvancedButton.Enum, ItemButton> renderItemHolders = Maps.newHashMap();

    public RecipeAppendix(IInfoBook infoBook, T recipe) {
        super(infoBook);
        this.recipe = recipe;
    }

    protected int getTick(GuiInfoBook gui) {
        return gui.getTick() / TICK_DELAY;
    }

    protected ItemStack prepareItemStacks(List<ItemStack> itemStacks, int tick) {
        if(itemStacks.size() == 0) return null;
        return prepareItemStack(itemStacks.get(tick % itemStacks.size()).copy(), tick);
    }

    protected ItemStack prepareItemStack(ItemStack itemStack, int tick) {
        if(itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            List<ItemStack> itemStacks = Lists.newLinkedList();
            itemStack.getItem().getSubItems(itemStack.getItem(), null, itemStacks);
            if(itemStacks.isEmpty()) return itemStack;
            return itemStacks.get(tick % itemStacks.size());
        }
        return itemStack;
    }

    protected void renderItem(GuiInfoBook gui, int x, int y, ItemStack itemStack, int mx, int my, AdvancedButton.Enum buttonEnum) {
        renderItem(gui, x, y, itemStack, mx, my, true, buttonEnum);
    }

    protected void renderItem(GuiInfoBook gui, int x, int y, ItemStack itemStack, int mx, int my, boolean renderOverlays, AdvancedButton.Enum buttonEnum) {
        if(renderOverlays) gui.drawOuterBorder(x, y, SLOT_SIZE, SLOT_SIZE, 1, 1, 1, 0.2f);

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        renderItem.renderItemAndEffectIntoGUI(itemStack, x, y);
        if(renderOverlays) renderItem.renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, itemStack, x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if(buttonEnum != null && renderOverlays) renderItemHolders.get(buttonEnum).update(x, y, itemStack, gui);
    }

    protected void renderItemTooltip(GuiInfoBook gui, int x, int y, ItemStack itemStack, int mx, int my) {
        GlStateManager.pushMatrix();
        if(mx >= x && my >= y && mx <= x + SLOT_SIZE && my <= y + SLOT_SIZE && itemStack != null ) {
            gui.renderToolTip(itemStack, mx, my);
        }
        GlStateManager.popMatrix();

        GlStateManager.disableLighting();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected int getHeight() {
        return getHeightInner() + getAdditionalHeight();
    }

    protected abstract int getHeightInner();

    protected int getAdditionalHeight() {
        return 5;
    }

    @Override
    protected int getOffsetY() {
        return getAdditionalHeight();
    }

    protected abstract String getUnlocalizedTitle();

    @Override
    public final void drawElement(GuiInfoBook gui, int x, int y, int width, int height, int page, int mx, int my) {
        int yOffset = getAdditionalHeight();
        gui.drawOuterBorder(x - 1, y - 1 - yOffset, getWidth() + 2, getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(L10NHelpers.localize(getUnlocalizedTitle()), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, Helpers.RGBToInt(120, 20, 30));

        drawElementInner(gui, x, y, width, height, page, mx, my);
    }

    protected void postDrawElement(GuiInfoBook gui, int x, int y, int width, int height, int page, int mx, int my) {
        renderToolTips(gui, mx, my);
    }

    protected abstract void drawElementInner(GuiInfoBook gui, int x, int y, int width, int height, int page, int mx, int my);

    protected void renderToolTips(GuiInfoBook gui, int mx, int my) {
        for(ItemButton renderItemHolder : renderItemHolders.values()) {
            renderItemTooltip(gui, renderItemHolder.xPosition, renderItemHolder.yPosition, renderItemHolder.getItemStack(), mx, my);
        }
    }

    @Override
    public void preBakeElement(InfoSection infoSection) {
        renderItemHolders.clear();
    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        infoSection.addAdvancedButtons(getPage(), renderItemHolders.values());
    }

    protected static class ItemButton extends AdvancedButton {

        @Getter private ItemStack itemStack;

        public ItemButton() {

        }

        /**
         * This is called each render tick to update the button to the latest render state.
         * @param x The X position.
         * @param y The Y position.
         * @param itemStack The itemStack to display.
         * @param gui The gui.
         */
        public void update(int x, int y, ItemStack itemStack, GuiInfoBook gui) {
            this.itemStack = itemStack;
            InfoSection target = null;
            if(this.itemStack != null) {
                ExtendedConfig<?> config = ConfigHandler.getConfigFromItem(itemStack.getItem());
                if (config != null) {
                    Pair<InfoSection, Integer> pair = InfoBookParser.configLinks.get(config.getFullUnlocalizedName());
                    if(pair != null) {
                        target = pair.getLeft();
                    }
                }
            }
            super.update(x, y, "empty", target, gui);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if(isVisible() && isHover(mouseX, mouseY)) {
                gui.drawOuterBorder(xPosition, yPosition, 16, 16, 0.392f, 0.392f, 0.6f, 0.9f);
            }
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && itemStack != null;
        }

    }

}
