package link.infra.demagnetize.blocks;

import link.infra.demagnetize.Demagnetize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class DemagnetizerGui extends ContainerScreen<DemagnetizerContainer> {
	private static final ResourceLocation background = new ResourceLocation(Demagnetize.MODID, "textures/gui/demagnetizer.png");
	private final DemagnetizerTileEntity te;

	private IconButton rsButton;
	private IconButton whitelistButton;

	private final boolean hasFilter;

	public DemagnetizerGui(DemagnetizerContainer inventorySlotsIn, PlayerInventory inv, ITextComponent name) {
		super(inventorySlotsIn, inv, name);

		this.te = inventorySlotsIn.te;
		xSize = 176;
		ySize = 166;

		hasFilter = te.getFilterSize() > 0;
	}

	@Override
	public void init() {
		super.init();

		addButton(new RangeSlider(guiLeft + 7, guiTop + 17, te.getMaxRange(), te.getRange()));

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
		rsButton = new IconButton(guiLeft + 124, guiTop + 17, rsStates, currentRSState, background, 0, 184) {
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
		addButton(rsButton);

		if (hasFilter) {
			String[] whitelistStates = {"blacklist", "whitelist"};
			int currentWhitelistState = te.isWhitelist() ? 1 : 0;
			whitelistButton = new IconButton(guiLeft + 148, guiTop + 17, whitelistStates, currentWhitelistState, background, 0, 204) {
				@Override
				public void updateState(int currentState) {
					te.setWhitelist(currentState == 1);
				}
			};
			addButton(whitelistButton);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		assert minecraft != null;
		minecraft.getTextureManager().bindTexture(background);
		blit(guiLeft, guiTop, 0, 0, 176, 166);
		for (int i = 0; i < te.getFilterSize(); i++) {
			blit(guiLeft + 7 + (i * 18), guiTop + 52, 0, 166, 18, 18);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String demagName = title.getFormattedText();
		int centeredPos = (xSize - font.getStringWidth(demagName)) / 2;
		font.drawString(demagName, centeredPos, 6, 0x404040);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8, ySize - 96 + 3, 0x404040);
		if (hasFilter) {
			font.drawString(I18n.format("label." + Demagnetize.MODID + ".demagnetizer.filter"), 8, 42, 0x404040);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);

		rsButton.renderTooltip(mouseX, mouseY);
		if (hasFilter) {
			whitelistButton.renderTooltip(mouseX, mouseY);
		}
	}

	@Override
	public void removed() {
		te.sendSettingsToServer();
		super.removed();
	}

	private class RangeSlider extends AbstractSlider {
		private int sliderValue;
		private final int maxValue;
		private final static int minValue = 1;

		RangeSlider(int x, int y, int maxRange, int value) {
			super(x, y, 113, 20, ((float) (value - minValue)) / (float) (maxRange - minValue));
			sliderValue = value;
			maxValue = maxRange;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			String rangeText = I18n.format("label." + Demagnetize.MODID + ".demagnetizer.range");
			setMessage(rangeText + ": " + sliderValue);
		}

		@Override
		protected void applyValue() {
			sliderValue = (int) (Math.round(value * (maxValue - minValue)) + minValue);
			te.setRange(sliderValue);
		}
	}

	public abstract class IconButton extends AbstractButton {
		private final String[] stateList;
		private int currentState;
		private final ResourceLocation location;
		private final int resourceX;
		private final int resourceY;

		IconButton(int x, int y, String[] stateList, int currentState, ResourceLocation location, int resourceX, int resourceY) {
			super(x, y, 20, 20, "");

			this.stateList = stateList;
			this.currentState = currentState;
			this.location = location;
			this.resourceX = resourceX;
			this.resourceY = resourceY;
		}

		@Override
		public void renderButton(int mouseX, int mouseY, float partialTicks) {
			super.renderButton(mouseX, mouseY, partialTicks);
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindTexture(location);
			blit(x, y, resourceX + currentState * width, resourceY, width, height);
		}

		void renderTooltip(int mouseX, int mouseY) {
			if (isHovered()) {
				DemagnetizerGui.this.renderTooltip(getNarrationMessage(), mouseX, mouseY);
			}
		}

		@Override
		@Nonnull
		protected String getNarrationMessage() {
			return I18n.format("label." + Demagnetize.MODID + ".demagnetizer." + stateList[currentState]);
		}

		@Override
		public void onPress() {
			currentState++;
			if (currentState >= stateList.length) {
				currentState = 0;
			}
			updateState(currentState);
		}

		public abstract void updateState(int currentState);
	}

	// Fix for mouse dragging with RangeSlider
	@Override
	public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
		boolean val = (this.getFocused() != null && this.isDragging() && p_mouseDragged_5_ == 0) && this.getFocused().mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
		if (val) {
			return true;
		}
		return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
	}

}
