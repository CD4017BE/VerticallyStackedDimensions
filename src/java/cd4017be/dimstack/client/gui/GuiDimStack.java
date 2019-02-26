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
	private GuiButton b_edit;
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
		dimList = addButton(new GuiList<>(1, 8, 36, 200, height - 72, translate("gui.dimstack.ldim")));
		List<Dim> list = dimList.list;
		Integer[] ids = DimensionManager.getStaticDimensionIDs();
		Arrays.sort(ids);
		for (int id : ids)
			list.add(new Dim(id));
		dimStack = addButton(new GuiList<>(2, width - 208, 36, 200, height - 72, translate("gui.dimstack.lstack")));
		addButton(new GuiButtonExt(3, 216, height / 2 - 38, width - 432, 20, translate("gui.dimstack.sel")));
		addButton(new GuiButtonExt(4, 216, height / 2 - 10, width - 432, 20, translate("gui.dimstack.add")));
		addButton(new GuiButtonExt(5, 216, height / 2 + 18, width - 432, 20, translate("gui.dimstack.rem")));
		b_edit = addButton(new GuiButton(6, width - 158, height - 28, 150, 20, translate("gui.dimstack.edit")));
		b_edit.enabled = false;
		setSelDimstack(selStack);
		textFields.add(new GuiTextField(7, fontRenderer, 8, height - 28, 70, 20));
		addButton(new GuiButtonExt(8, 78, height - 28, 80, 20, translate("gui.dimstack.search")));
	}

	private void setSelDimstack(PortalConfiguration dim) {
		selStack = dim;
		if (dim == null) {
			dimStack.list.clear();
			dimStack.sel = -1;
			b_edit.enabled = false;
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
		b_edit.enabled = true;
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		int s;
		List<Dim> list;
		switch(b.id) {
		case 2:
			b_edit.enabled = dimStack.sel >= 0;
			break;
		case 3:
			s = dimList.sel;
			list = dimList.list;
			setSelDimstack(s >= 0 && s < list.size() ? list.get(s).getDim() : null);
			break;
		case 4:
			s = dimList.sel;
			list = dimList.list;
			if (s >= 0 && s < list.size()) {
				PortalConfiguration d1 = list.get(s).getDim();
				s = dimStack.sel;
				list = dimStack.list;
				if (list.isEmpty()) break;
				if (s < 0) {
					PortalConfiguration d = list.get(0).getDim();
					d.insertTop(d1);
				} else {
					PortalConfiguration d = list.get(s).getDim();
					d.insertBottom(d1);
				}
				setSelDimstack(d1);
			}
			break;
		case 5:
			s = dimStack.sel;
			list = dimStack.list;
			if (s >= 0 && s < list.size()) {
				PortalConfiguration d = list.get(s).getDim(), d1 = d.up();
				if (d1 == null) d1 = d.down();
				d.unlink();
				setSelDimstack(d1);
				for (int i = dimList.list.size() - 1; i >= 0; i--)
					if (dimList.list.get(i).id == d.dimId) {
						dimList.sel = i;
						break;
					}
			}
			break;
		case 6:
			s = dimStack.sel;
			list = dimStack.list;
			if (s >= 0 && s < list.size())
				mc.displayGuiScreen(new GuiEditDim(this, list.get(s).getDim()));
			break;
		case 8:
			try {
				int i = Integer.parseInt(textFields.get(0).getText());
				list = dimList.list;
				s = 0;
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
