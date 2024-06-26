package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.client.gui.container.ContainerCellLink;
import com.asdflj.ae2thing.inventory.ItemCellLinkInventory;
import com.asdflj.ae2thing.util.NameConst;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;

public class GuiCellLink extends AEBaseGui {

    public GuiCellLink(InventoryPlayer inventory, ItemCellLinkInventory inv) {
        super(new ContainerCellLink(inventory, inv));
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_CELL_LINK)), 8, 6, 0x404040);
        fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, ySize - 94, 0x404040);
    }

    public void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(AE2Thing.MODID, "textures/" + file);
        this.mc.getTextureManager()
            .bindTexture(loc);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTextureBack(this.getBackground());
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
    }

    protected String getBackground() {
        return "gui/cell_link.png";
    }
}
