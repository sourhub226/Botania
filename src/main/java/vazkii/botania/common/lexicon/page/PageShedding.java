/**
 * This class was created by <SoundLogic>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jul 4, 2014, 10:38:50 PM (GMT)]
 */
package vazkii.botania.common.lexicon.page;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.internal.IGuiLexiconEntry;
import vazkii.botania.api.lexicon.LexiconRecipeMappings;
import vazkii.botania.api.lexicon.LexiconRecipeMappings.EntryData;
import vazkii.botania.client.gui.lexicon.GuiLexiconEntry;
import vazkii.botania.client.lib.LibResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageShedding extends PageEntity {

	private static final ResourceLocation sheddingOverlay = new ResourceLocation(LibResources.GUI_SHEDDING_OVERLAY);

	private final ItemStack shedStack;
	private ItemStack tooltipStack;
	private boolean tooltipEntry;

	private static boolean mouseDownLastTick = false;

	public PageShedding(String unlocalizedName, EntityType<?> type, int size, ItemStack shedStack) {
		super(unlocalizedName, type, size);
		this.shedStack = shedStack;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderScreen(IGuiLexiconEntry gui, int mx, int my) {
		prepDummy();
		relativeMouseX = mx;
		relativeMouseY = my;
		int stack_x = gui.getLeft() + gui.getWidth() / 2 - 8;
		int stack_y = gui.getTop() + gui.getHeight() - 40 - 18 - 5;
		int entity_scale = getEntityScale(size);
		int entity_x = gui.getLeft() + gui.getWidth() / 2;
		int entity_y = gui.getTop() + gui.getHeight() / 2 + MathHelper.floor(dummyEntity.height * entity_scale / 2) - 29;

		renderEntity(gui, dummyEntity, entity_x, entity_y, entity_scale, dummyEntity.ticksExisted * 2);

		renderItem(gui, stack_x, stack_y, shedStack);

		TextureManager render = Minecraft.getInstance().textureManager;
		render.bindTexture(sheddingOverlay);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		((GuiScreen) gui).drawTexturedModalRect(gui.getLeft(), gui.getTop(), 0, 0, gui.getWidth(), gui.getHeight());

		if(!tooltipStack.isEmpty()) {
			List<ITextComponent> tooltipData = tooltipStack.getTooltip(Minecraft.getInstance().player, ITooltipFlag.TooltipFlags.NORMAL);
			List<String> parsedTooltip = new ArrayList<>();
			boolean first = true;

			for(ITextComponent s : tooltipData) {
				String s_ = s.getFormattedText();
				if(!first)
					s_ = TextFormatting.GRAY + s.getFormattedText();
				parsedTooltip.add(s_);
				first = false;
			}

			vazkii.botania.client.core.helper.RenderHelper.renderTooltip(mx, my, parsedTooltip);

			int tooltipY = 8 + tooltipData.size() * 11;

			if(tooltipEntry) {
				vazkii.botania.client.core.helper.RenderHelper.renderTooltipOrange(mx, my + tooltipY, Collections.singletonList(TextFormatting.GRAY + I18n.format("botaniamisc.clickToRecipe")));
			}
		}
		else if(tooltipEntity) {
			List<String> parsedTooltip = new ArrayList<>();
			parsedTooltip.add(I18n.format(dummyEntity.getType().getTranslationKey()));
			vazkii.botania.client.core.helper.RenderHelper.renderTooltip(mx, my, parsedTooltip);
		}

		tooltipStack = null;
		tooltipEntry = tooltipEntity = false;
		GlStateManager.disableBlend();
		mouseDownLastTick = GLFW.glfwGetMouseButton(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
	}

	@OnlyIn(Dist.CLIENT)
	public void renderItem(IGuiLexiconEntry gui, int xPos, int yPos, ItemStack stack) {
		ItemRenderer render = Minecraft.getInstance().getItemRenderer();
		boolean mouseDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableDepthTest();
		render.renderItemAndEffectIntoGUI(stack, xPos, yPos);
		render.renderItemOverlays(Minecraft.getInstance().fontRenderer, stack, xPos, yPos);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popMatrix();

		if(relativeMouseX >= xPos && relativeMouseY >= yPos && relativeMouseX <= xPos + 16 && relativeMouseY <= yPos + 16) {
			tooltipStack = stack;

			EntryData data = LexiconRecipeMappings.getDataForStack(tooltipStack);
			if(data != null && (data.entry != gui.getEntry() || data.page != gui.getPageOn())) {
				tooltipEntry = true;

				if(!mouseDownLastTick && mouseDown && GuiScreen.isShiftKeyDown()) {
					GuiLexiconEntry newGui = new GuiLexiconEntry(data.entry, (GuiScreen) gui);
					newGui.page = data.page;
					Minecraft.getInstance().displayGuiScreen(newGui);
				}
			}
		}

		GlStateManager.disableLighting();
	}

	@Override
	public List<ItemStack> getDisplayedRecipes() {
		ArrayList<ItemStack> list = new ArrayList<>();
		list.add(shedStack);
		return list;
	}

}
