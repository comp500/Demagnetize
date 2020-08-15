package link.infra.demagnetize.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import link.infra.demagnetize.Demagnetize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	protected void drawGuiContainerBackgroundLayer(@Nonnull MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		assert minecraft != null;
		minecraft.getTextureManager().bindTexture(background);
		blit(stack, guiLeft, guiTop, 0, 0, 176, 166);
		for (int i = 0; i < te.getFilterSize(); i++) {
			blit(stack, guiLeft + 7 + (i * 18), guiTop + 52, 0, 166, 18, 18);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(@Nonnull MatrixStack stack, int mouseX, int mouseY) {
		String demagName = title.getString();
		int centeredPos = (xSize - font.getStringWidth(demagName)) / 2;
		font.drawString(stack, demagName, centeredPos, 6, 0x404040);
		font.drawString(stack, playerInventory.getDisplayName().getString(), 8, ySize - 96 + 3, 0x404040);
		if (hasFilter) {
			font.drawString(stack, I18n.format("label." + Demagnetize.MODID + ".demagnetizer.filter"), 8, 42, 0x404040);
		}
	}

	@Override
	public void render(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		renderHoveredTooltip(stack, mouseX, mouseY);

		rsButton.renderTooltip(stack, mouseX, mouseY);
		if (hasFilter) {
			whitelistButton.renderTooltip(stack, mouseX, mouseY);
		}
	}

	@Override
	public void closeScreen() {
		te.sendSettingsToServer();
		super.closeScreen();
	}

	private class RangeSlider extends AbstractSlider {
		private int scaledValue;
		private final int maxValue;
		private final static int minValue = 1;

		RangeSlider(int x, int y, int maxRange, int value) {
			super(x, y, 113, 20, StringTextComponent.EMPTY, ((float) (value - minValue)) / (float) (maxRange - minValue));
			scaledValue = value;
			maxValue = maxRange;
			func_230979_b_();
		}

		// How did this get unmapped?? thanks MCP
		// Was previously matched to updateMessage
		@Override
		protected void func_230979_b_() {
			setMessage(new TranslationTextComponent("label." + Demagnetize.MODID + ".demagnetizer.range").appendString(": " + scaledValue));
		}

		// Was previously matched to applyValue
		@Override
		protected void func_230972_a_() {
			scaledValue = (int) (Math.round(sliderValue * (maxValue - minValue)) + minValue);
			te.setRange(scaledValue);
		}
	}

	public abstract class IconButton extends AbstractButton {
		private final String[] stateList;
		private int currentState;
		private final ResourceLocation location;
		private final int resourceX;
		private final int resourceY;

		IconButton(int x, int y, String[] stateList, int currentState, ResourceLocation location, int resourceX, int resourceY) {
			super(x, y, 20, 20, StringTextComponent.EMPTY);

			this.stateList = stateList;
			this.currentState = currentState;
			this.location = location;
			this.resourceX = resourceX;
			this.resourceY = resourceY;
		}

		@Override
		public void renderButton(@Nonnull MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(stack, mouseX, mouseY, partialTicks);
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindTexture(location);
			blit(stack, x, y, resourceX + currentState * width, resourceY, width, height);
		}

		void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
			if (isHovered()) {
				DemagnetizerGui.this.renderTooltip(stack, getNarrationMessage(), mouseX, mouseY);
			}
		}

		@Override
		@Nonnull
		protected IFormattableTextComponent getNarrationMessage() {
			return new TranslationTextComponent("label." + Demagnetize.MODID + ".demagnetizer." + stateList[currentState]);
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
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.getListener() != null && this.isDragging() && button == 0 && this.getListener().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

}
