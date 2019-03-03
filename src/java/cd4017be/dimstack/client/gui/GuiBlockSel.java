package cd4017be.dimstack.client.gui;

import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.client.RenderUtil;
import cd4017be.dimstack.client.RenderUtil.BlockWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;


/**
 * @author CD4017BE
 *
 */
public class GuiBlockSel extends GuiTextField {

	private final BlockStateCompletion complete;
	private final FontRenderer fontRenderer;
	private int sel;

	public GuiBlockSel(int id, FontRenderer fontrenderer, BlockStateCompletion compl, int x, int y, int w, int h) {
		super(id, fontrenderer, x, y, w, h);
		this.complete = compl;
		this.fontRenderer = fontrenderer;
	}

	@Override
	public void drawTextBox() {
		super.drawTextBox();
		if (getVisible()) {
			int x = this.x + width - height, y = this.y, w = height;
			IBlockState state = BlockPredicate.parse(getText());
			GlStateManager.pushMatrix();
			float h = (float)w / 2F, s = (float)w * 0.71F;
			GlStateManager.translate(x + h, y + h, zLevel);
			GlStateManager.scale(s, -s, s);
			GlStateManager.rotate(15, 1, 1, 0);
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);
			BlockWrapper world = new BlockWrapper(state);
			BufferBuilder bb = Tessellator.getInstance().getBuffer();
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			RenderUtil.renderBlock(world, BlockPos.ORIGIN, bb);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			Tessellator.getInstance().draw();
			GlStateManager.popMatrix();
		}
	}

	public void drawOverlay() {
		if (!getVisible() || !isFocused()) return;
		int x = this.x, y = this.y + height;
		ArrayList<String> list = new ArrayList<>();
		int w = 0;
		for (String s : complete.getResults())
			if (s != null) {
				list.add(s);
				w = Math.max(w, fontRenderer.getStringWidth(s));
			} else break;
		if (list.isEmpty()) sel = 0;
		else {
			drawRect(x, y, x + w, y + list.size() * fontRenderer.FONT_HEIGHT, 0x80000000);
			if (sel >= list.size()) sel = list.size() - 1;
			int y1 = y + sel * fontRenderer.FONT_HEIGHT;
			drawRect(x, y1, x + w, y1 + fontRenderer.FONT_HEIGHT, 0x80408080);
			for (String s : list) {
				fontRenderer.drawString(s, x, y, 0xffffff);
				y += fontRenderer.FONT_HEIGHT;
			}
		}
	}

	@Override
	public int getWidth() {
		return super.getWidth() - height;
	}

	@Override
	public boolean textboxKeyTyped(char c, int k) {
		if (!isFocused()) return false;
		switch(k) {
		case Keyboard.KEY_TAB:
		case Keyboard.KEY_RETURN:
			String[] entries = complete.getResults();
			if (sel < entries.length) {
				String s = entries[sel];
				if (s != null) {
					setText(s);
					setResponderEntryValue(getId(), s);
					setCursorPosition(s.length());
					updateCompletion();
				}
			}
			return true;
		case Keyboard.KEY_UP:
			if (sel > 0) sel--;
			return true;
		case Keyboard.KEY_DOWN:
			sel++;
			return true;
		default:
			if (super.textboxKeyTyped(c, k)) {
				updateCompletion();
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
			if (isFocused()) updateCompletion();
			return true;
		}
		return false;
	}

	private void updateCompletion() {
		String text = getText();
		int i = getCursorPosition();
		complete.updateText(text.substring(0, i));
	}

}
