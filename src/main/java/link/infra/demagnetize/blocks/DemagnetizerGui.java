package link.infra.demagnetize.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import link.infra.demagnetize.Demagnetize;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class DemagnetizerGui extends AbstractContainerScreen<DemagnetizerContainer> {
	private static final ResourceLocation background = new ResourceLocation(Demagnetize.MODID, "textures/gui/demagnetizer.png");
	private final DemagnetizerTileEntity te;

	private IconButton rsButton;
	private IconButton whitelistButton;

	private final boolean hasFilter;

	public DemagnetizerGui(DemagnetizerContainer inventorySlotsIn, Inventory inv, Component name) {
		super(inventorySlotsIn, inv, name);

		this.te = inventorySlotsIn.te;
		imageWidth = 176;
		imageHeight = 166;

		hasFilter = te.getFilterSize() > 0;
	}

	@Override
	public void init() {
		super.init();

		addRenderableWidget(new RangeSlider(leftPos + 7, topPos + 17, te.getMaxRange(), te.getRange()));

		String[] rsStates = {"rsignored", "rson", "rsoff"};
		int currentRSState;
		switch (te.getRedstoneSetting()) {
			case POWERED -> currentRSState = 1;
			case UNPOWERED -> currentRSState = 2;
			default -> currentRSState = 0;
		}
		rsButton = new IconButton(leftPos + 124, topPos + 17, rsStates, currentRSState, background, 0, 184) {

			@Override
			public void updateState(int currentState) {
				switch (currentState) {
					case 0 -> te.setRedstoneSetting(DemagnetizerTileEntity.RedstoneStatus.REDSTONE_DISABLED);
					case 1 -> te.setRedstoneSetting(DemagnetizerTileEntity.RedstoneStatus.POWERED);
					case 2 -> te.setRedstoneSetting(DemagnetizerTileEntity.RedstoneStatus.UNPOWERED);
				}
			}
		};
		addRenderableWidget(rsButton);

		if (hasFilter) {
			String[] whitelistStates = {"blacklist", "whitelist"};
			int currentWhitelistState = te.isWhitelist() ? 1 : 0;
			whitelistButton = new IconButton(leftPos + 148, topPos + 17, whitelistStates, currentWhitelistState, background, 0, 204) {
				@Override
				public void updateState(int currentState) {
					te.setWhitelist(currentState == 1);
				}
			};
			addRenderableWidget(whitelistButton);
		}
	}

	@Override
	protected void renderBg(@Nonnull PoseStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, background);
		blit(stack, leftPos, topPos, 0, 0, 176, 166);
		for (int i = 0; i < te.getFilterSize(); i++) {
			blit(stack, leftPos + 7 + (i * 18), topPos + 52, 0, 166, 18, 18);
		}
	}

	@Override
	protected void renderLabels(@Nonnull PoseStack stack, int mouseX, int mouseY) {
		String demagName = title.getString();
		int centeredPos = (imageWidth - font.width(demagName)) / 2;
		font.draw(stack, demagName, centeredPos, 6, 0x404040);
		font.draw(stack, playerInventoryTitle, 8, imageHeight - 96 + 3, 0x404040);
		if (hasFilter) {
			font.draw(stack, I18n.get("label." + Demagnetize.MODID + ".demagnetizer.filter"), 8, 42, 0x404040);
		}
	}

	@Override
	public void render(@Nonnull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		renderTooltip(stack, mouseX, mouseY);

		rsButton.renderTooltip(stack, mouseX, mouseY);
		if (hasFilter) {
			whitelistButton.renderTooltip(stack, mouseX, mouseY);
		}
	}

	@Override
	public void onClose() {
		te.sendSettingsToServer();
		super.onClose();
	}

	private class RangeSlider extends AbstractSliderButton {
		private int scaledValue;
		private final int maxValue;
		private final static int minValue = 1;

		RangeSlider(int x, int y, int maxRange, int value) {
			super(x, y, 113, 20, TextComponent.EMPTY, ((float) (value - minValue)) / (float) (maxRange - minValue));
			scaledValue = value;
			maxValue = maxRange;
			visible = true;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(new TranslatableComponent("label." + Demagnetize.MODID + ".demagnetizer.range").append(": " + scaledValue));
		}

		@Override
		protected void applyValue() {
			scaledValue = (int) (Math.round(value * (maxValue - minValue)) + minValue);
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
			super(x, y, 20, 20, TextComponent.EMPTY);

			this.stateList = stateList;
			this.currentState = currentState;
			this.location = location;
			this.resourceX = resourceX;
			this.resourceY = resourceY;
			visible = true;
		}

		@Override
		public void updateNarration(@Nonnull NarrationElementOutput output) {
			//TODO maybe actually do something here
		}

		@Override
		public void renderButton(@Nonnull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(stack, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderTexture(0, location);
			blit(stack, x, y, resourceX + currentState * width, resourceY, width, height);
		}

		void renderTooltip(PoseStack stack, int mouseX, int mouseY) {
			if (isHovered) {
				DemagnetizerGui.this.renderTooltip(stack, createNarrationMessage(), mouseX, mouseY);
			}
		}

		@Override
		@Nonnull
		protected MutableComponent createNarrationMessage() {
			return new TranslatableComponent("label." + Demagnetize.MODID + ".demagnetizer." + stateList[currentState]);
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
		if (this.getFocused() != null && this.isDragging() && button == 0 && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

}
