package cd4017be.dimstack.client.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cd4017be.dimstack.core.PortalConfiguration;
import static cd4017be.lib.util.TooltipUtil.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.config.GuiButtonExt;

/**
 * @author CD4017BE
 *
 */
public class GuiDimStack extends GuiMenuBase {

	private GuiList<Dim> dimList, dimStack;
	private GuiButton b_edit, b_sel, b_add, b_rem, b_cut;
	private PortalConfiguration selStack;

	/**
	 * @param parent
	 */
	public GuiDimStack(GuiScreen parent) {
		super(parent);
	}

	@Override
	public void initGui() {
		super.initGui();
		title = translate("gui.dimstack.cfg");
		
		int bw = 70, w = (width - 32 - bw) / 2;
		if (w > 200) {
			w = 200;
			bw = width - 2 * w - 32;
			if (bw > 100) bw = 100;
		}
		int left = (width - bw - 16) / 2 - w, right = (width + bw + 16) / 2 + w;
		
		dimList = addButton(new GuiList<>(1, (width - bw - 16) / 2 - w, 36, w, height - 72, translate("gui.dimstack.ldim")));
		List<Dim> list = dimList.list;
		Integer[] ids = DimensionManager.getStaticDimensionIDs();
		Arrays.sort(ids);
		for (int id : ids)
			list.add(new Dim(id));
		dimStack = addButton(new GuiList<>(2, (width + bw + 16) / 2, 36, w, height - 72, translate("gui.dimstack.lstack")));
		int y = (height - 4 * 28 + 8) / 2, x = (width - bw) / 2;
		b_sel = addButton(new GuiButtonExt(3, x, y, bw, 20, translate("gui.dimstack.sel")));
		b_add = addButton(new GuiButtonExt(4, x, y += 28, bw, 20, translate("gui.dimstack.add")));
		b_rem = addButton(new GuiButtonExt(5, x, y += 28, bw, 20, translate("gui.dimstack.rem")));
		b_cut = addButton(new GuiButtonExt(9, x, y += 28, bw, 20, translate("gui.dimstack.cut")));
		bw = Math.min(150, (width - 32) / 3);
		b_edit = addButton(new GuiButtonExt(6, right - bw, height - 28, bw, 20, translate("gui.dimstack.edit")));
		setSelDimstack(selStack);
		textFields.add(new GuiTextField(7, fontRenderer, left, height - 28, 40, 20));
		addButton(new GuiButtonExt(8, left + 40, height - 28, bw - 40, 20, translate("gui.dimstack.search")));
		if (bw < 150) {
			b_close.setWidth(bw);
			b_close.x += (150 - bw) / 2;
		}
	}

	private void setSelDimstack(PortalConfiguration dim) {
		selStack = dim;
		if (dim == null) {
			dimStack.list.clear();
			dimStack.sel = -1;
			updateButtons();
			return;
		}
		List<Dim> list = dimStack.list;
		list.clear();
		PortalConfiguration d = dim, d1 = d.top();
		if (d1 == null) d1 = d;
		d = d1;
		list.add(new Dim(d.dimId));
		dimStack.sel = 0;
		for (d = d.down(); d != null; d = d.down()) {
			if (d == d1) break;
			if (d == dim) dimStack.sel = list.size();
			list.add(new Dim(d.dimId));
		}
		updateButtons();
	}

	private void updateButtons() {
		boolean bs = selStack != null, bx = !dimStack.list.isEmpty();
		boolean bl = dimList.getSelEl() != null;
		b_edit.enabled = bs;
		b_rem.enabled = bs;
		b_cut.enabled = bs && selStack.down() != null;
		b_add.enabled = bl && bx;
		b_sel.enabled = bl;
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		Dim sd;
		switch(b.id) {
		case 1:
			updateButtons();
			break;
		case 2:
			sd = dimStack.getSelEl();
			selStack = sd == null ? null : sd.getDim();
			updateButtons();
			break;
		case 3:
			sd = dimList.getSelEl();
			setSelDimstack(sd != null ? sd.getDim() : null);
			break;
		case 4:
			sd = dimList.getSelEl();
			if (sd != null) {
				PortalConfiguration d1 = sd.getDim();
				if (dimStack.list.isEmpty()) break;
				if (selStack == null)
					dimStack.list.get(0).getDim().insertTop(d1);
				else selStack.insertBottom(d1);
				setSelDimstack(d1);
			}
			break;
		case 5:
			if (selStack != null) {
				PortalConfiguration d = selStack, d1 = d.up();
				if (d1 == null) d1 = d.down();
				d.unlink();
				for (int i = dimList.list.size() - 1; i >= 0; i--)
					if (dimList.list.get(i).id == d.dimId) {
						dimList.sel = i;
						break;
					}
				setSelDimstack(d1);
			}
			break;
		case 6:
			if (selStack != null)
				mc.displayGuiScreen(new GuiEditDim(this, selStack));
			break;
		case 8:
			try {
				int i = Integer.parseInt(textFields.get(0).getText());
				List<Dim> list = dimList.list;
				int s = 0;
				for (int l = list.size(); s < l; s++) {
					int id = list.get(s).id;
					if (id == i) {
						dimList.sel = s;
						return;
					}
					if (id > i) break;
				}
				list.add(s, new Dim(i));
				dimList.sel = s;
			} catch (NumberFormatException e) {}
			textFields.get(0).setText("");
			break;
		case 9:
			if (selStack != null) {
				selStack.splitBottom();
				setSelDimstack(selStack);
			}
			break;
		default: super.actionPerformed(b);
		}
	}

	static class Dim implements IDrawableEntry {

		final int id;

		public Dim(int id) {
			this.id = id;
		}

		@Override
		public void draw(Minecraft mc, int x, int y, int w, int h, float t) {
			PortalConfiguration pc = PortalConfiguration.get(id);
			mc.fontRenderer.drawString(pc.toString(), x + 2, y + (h - mc.fontRenderer.FONT_HEIGHT) / 2, 0xffff80);
			int y0 = y + h / 2 - 1, y1 = y0 + 1;
			boolean u = pc.up() != null, d = pc.down() != null;
			if (!(u && d))
				drawRect(x + w - 8, y0, x + w - 3, y1, 0xff00ff00);
			if (u) y0 = y + 1;
			if (d) y1 = y + h - 2;
			drawRect(x + w - 6, y0, x + w - 5, y1, 0xff00ff00);
		}

		PortalConfiguration getDim() {
			return PortalConfiguration.get(id);
		}

	}

}
