package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class DemagnetizerGui extends GuiContainer {
	
	public static final int GUI_ID = 1;
	private static final ResourceLocation background = new ResourceLocation(Demagnetize.MODID, "textures/gui/demagnetizer.png");
	private DemagnetizerTileEntity te;
	
	private IconButton rsButton;
	private IconButton whitelistButton;

	public DemagnetizerGui(DemagnetizerTileEntity te, DemagnetizerContainer inventorySlotsIn) {
		super(inventorySlotsIn);
		
		this.te = te;
		xSize = 176;
		ySize = 166;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		buttonList.add(new RangeSlider(0, guiLeft + 7, guiTop + 17, te.getMaxRange(), te.getRange()));
		
		String[] rsStates = {"rsignored", "rson", "rsoff"};
		int currentRSState;
		switch (te.getRedstoneSetting()) {
		case POWERED:
			currentRSState = 1;
			break;
		case UNPOWERED:
			currentRSState = 2;
			break;
		case REDSTONE_DISABLED:
		default:
			currentRSState = 0;
		}
		rsButton = new IconButton(1, guiLeft + 124, guiTop + 17, rsStates, currentRSState, background, 0, 184) {
			@Override
			public void updateState(int currentState) {
				switch (currentState) {
				case 0:
					te.setRedstoneSetting(DemagnetizerTileEntity.RedstoneStatus.REDSTONE_DISABLED);
					break;
				case 1:
					te.setRedstoneSetting(DemagnetizerTileEntity.RedstoneStatus.POWERED);
					break;
				case 2:
					te.setRedstoneSetting(DemagnetizerTileEntity.RedstoneStatus.UNPOWERED);
					break;
				}
			}
		};
		buttonList.add(rsButton);
		
		String[] whitelistStates = {"blacklist", "whitelist"};
		int currentWhitelistState = te.isWhitelist() ? 1 : 0;
		whitelistButton = new IconButton(2, guiLeft + 148, guiTop + 17, whitelistStates, currentWhitelistState, background, 0, 204) {
			@Override
			public void updateState(int currentState) {
				te.setWhitelist(currentState == 1);
			}
		};
		buttonList.add(whitelistButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 166);
		for (int i = 0; i < te.getFilterSize(); i++) {
			int row = i / 4;
			int column = i % 4;
			drawTexturedModalRect(guiLeft + 7 + (column * 18), guiTop + 52 + (row * 18), 0, 166, 18, 18);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String demagName = I18n.format(te.getBlockType().getUnlocalizedName() + ".name");
		int centeredPos = (xSize - fontRenderer.getStringWidth(demagName)) / 2;
		fontRenderer.drawString(demagName, centeredPos, 6, 0x404040);
		fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 3, 0x404040);
		fontRenderer.drawString(I18n.format("label." + Demagnetize.MODID + ".demagnetizer.filter.name"), 8, 42, 0x404040);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
		
		rsButton.renderTooltip(mouseX, mouseY);
		whitelistButton.renderTooltip(mouseX, mouseY);
	}
	
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			te.setRange(((RangeSlider) button).getValue());
			break;
		case 1:
		case 2:
			((IconButton) button).handleClick();
		}
	}
	
	@Override
	public void onGuiClosed() {
		te.sendSettingsToServer();
	}

	private class RangeSlider extends GuiButton {

		private int sliderValue;
		public boolean dragging;
		private final int maxValue;
		private final int minValue = 1;

		public RangeSlider(int buttonId, int x, int y, int maxRange, int value) {
			super(buttonId, x, y, 113, 20, "");
			this.sliderValue = value;
			this.maxValue = maxRange;
			setDisplayString();
		}

		// From GuiOptionSlider. I don't know why it's needed.
		protected int getHoverState(boolean mouseOver) {
			return 0;
		}
		
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			if (this.visible) {
				if (this.dragging) {
					float mouseValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
					if (mouseValue > 1F) {
						mouseValue = 1F;
					} else if (mouseValue < 0F) {
						mouseValue = 0F;
					}
					this.sliderValue = Math.round(mouseValue * (maxValue - minValue)) + minValue;
					setDisplayString();
					te.setRange(sliderValue);
				}

				mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				float flValue = ((float)this.sliderValue - minValue) / (maxValue - minValue);
				this.drawTexturedModalRect(this.x + (int) (flValue * (float) (this.width - 8)), this.y, 0, 66,
						4, 20);
				this.drawTexturedModalRect(this.x + (int) (flValue * (float) (this.width - 8)) + 4, this.y,
						196, 66, 4, 20);
			}
		}
		
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			if (super.mousePressed(mc, mouseX, mouseY)) {
				float mouseValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
				this.sliderValue = Math.round(mouseValue * (maxValue - minValue)) + minValue;
				setDisplayString();
				te.setRange(sliderValue);
				
				this.dragging = true;
				return true;
			} else {
				return false;
			}
		}

		public void mouseReleased(int mouseX, int mouseY) {
			this.dragging = false;
		}
		
		public void setDisplayString() {
			String rangeText = I18n.format("label." + Demagnetize.MODID + ".demagnetizer.range.name");
			this.displayString = rangeText + ": " + sliderValue;
		}
		
		public int getValue() {
			return sliderValue;
		}

	}
	
	public abstract class IconButton extends GuiButton {
		private final String[] stateList;
		private int currentState;
		private final ResourceLocation location;
		private final int resourceX;
		private final int resourceY;

		public IconButton(int buttonId, int x, int y, String[] stateList, int currentState, ResourceLocation location, int resourceX, int resourceY) {
			super(buttonId, x, y, 20, 20, "");
			
			this.stateList = stateList;
			this.currentState = currentState;
			this.location = location;
			this.resourceX = resourceX;
			this.resourceY = resourceY;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			super.drawButton(mc, mouseX, mouseY, partialTicks);
			if (visible) {
				mc.getTextureManager().bindTexture(location);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				drawTexturedModalRect(this.x, this.y, resourceX + currentState * this.width, resourceY, this.width, this.height);
			}
		}
		
		public void renderTooltip(int mouseX, int mouseY) {
			if (isMouseOver()) {
				drawHoveringText(I18n.format("label." + Demagnetize.MODID + ".demagnetizer." + stateList[currentState] + ".name"), mouseX, mouseY);
			}
		}
		
		public void handleClick() {
			currentState++;
			if (currentState >= stateList.length) {
				currentState = 0;
			}
			updateState(currentState);
		}
		
		public abstract void updateState(int currentState);
		
	}

}
