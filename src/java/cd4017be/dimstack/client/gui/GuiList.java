package cd4017be.dimstack.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;


/**
 * @author CD4017BE
 *
 */
public class GuiList<T extends IDrawableEntry> extends GuiButton {

	public final List<T> list;
	final int maxEntries, entryHgt;
	public int scroll = 0, sel = -1;

	/**
	 * @param id
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param header
	 */
	public GuiList(int id, int x, int y, int w, int h, String header) {
		this(id, x, y, w, h, 16, header, new ArrayList<>());
	}

	/**
	 * @param id
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param entryHgt
	 * @param header
	 * @param list
	 */
	public GuiList(int id, int x, int y, int w, int h, int entryHgt, String header, List<T> list) {
		super(id, x, y, w, h, header);
		this.entryHgt = entryHgt;
		this.maxEntries = (h - 20) / entryHgt;
		this.list = list;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			FontRenderer fr = mc.fontRenderer;
			drawRect(x, y, x + width, y + height, 0xff000000);
			drawHorizontalLine(x, x + width - 1, y, 0xffffffff);
			drawHorizontalLine(x, x + width - 1, y + 19, 0xffffffff);
			drawHorizontalLine(x, x + width - 1, y + height - 1, 0xffffffff);
			drawVerticalLine(x, y, y + height - 1, 0xffffffff);
			drawVerticalLine(x + width - 1, y, y + height - 1, 0xffffffff);
			
			drawCenteredString(fr, displayString, x + width / 2, y + (20 - fr.FONT_HEIGHT) / 2, 0xffffff);
			
			int s = scroll, n = Math.min(maxEntries, list.size() - scroll);
			for (int i = 0; i < n; i++) {
				int y1 = y + 20 + entryHgt * i;
				if (i < maxEntries - 1)
					drawHorizontalLine(x + 1, x + width - 2, y1 + entryHgt - 1, 0xff404040);
				if (sel == i + s)
					drawRect(x + 1, y1, x + width - 1, y1 + entryHgt - 1, 0xff408080);
				IDrawableEntry e = list.get(i + s);
				if (e != null) e.draw(mc, x, y1, width, entryHgt, partialTicks);
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my) {
		if (!super.mousePressed(mc, mx, my)) return false;
		int dy = Math.floorDiv(my - y - 20, entryHgt);
		if (dy < 0 || dy >= maxEntries || (dy += scroll) >= list.size()) dy = -1;
		if (dy == sel) return false;
		sel = dy;
		return true;
	}

	public T getSelEl() {
		return sel >= 0 && sel < list.size() ? list.get(sel) : null;
	}

}
