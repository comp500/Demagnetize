package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class DemagnetizerGui extends GuiContainer {
	
	public static final int GUI_ID = 1;
	private static final ResourceLocation background = new ResourceLocation(Demagnetize.MODID, "textures/gui/demagnetizer.png");
	private DemagnetizerTileEntity te;

	public DemagnetizerGui(DemagnetizerTileEntity te, DemagnetizerContainer inventorySlotsIn) {
		super(inventorySlotsIn);
		
		this.te = te;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 166);
		for (int i = 0; i < te.getFilterSize(); i++) {
			int row = i / 4;
			int column = i % 4;
			drawTexturedModalRect(guiLeft + 69 + (column * 18), guiTop + 25 + (row * 18), 0, 166, 18, 18);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

}
