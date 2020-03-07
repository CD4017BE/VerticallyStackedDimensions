package cd4017be.dimstack.client.gui;

import static cd4017be.lib.util.TooltipUtil.format;
import static cd4017be.lib.util.TooltipUtil.translate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cd4017be.dimstack.api.BlockReplacements;
import cd4017be.dimstack.api.BlockReplacements.Replacement;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.client.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Blocks;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraftforge.fml.client.config.GuiButtonExt;

/**
 * @author CD4017BE
 *
 */
public class GuiBlockReplacement extends GuiMenuBase implements GuiResponder {

	private static final int WIDTH = 308, HEIGHT = 7*24 - 4 + 20;
	private final BlockReplacements cfg;
	private BlockStateCompletion complete;
	private GuiBlockSel block, old;
	private GuiTextField maxY, minY;
	private GuiButton rem;
	private GuiList<Repl> list;

	public GuiBlockReplacement(GuiScreen parent, IDimension dim) {
		super(parent);
		cfg = dim.getSettings(BlockReplacements.class, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.complete = AutoCompletions.blockstates();
		title = translate("gui.dimstack.replace");
		int x = (width - WIDTH) / 2, y = (height - HEIGHT) / 2;
		
		List<Repl> layers;
		if (list != null) layers = list.list;
		else {
			layers = new ArrayList<>();
			for (Replacement gen : cfg.replacements)
				layers.add(new Repl(gen));
		}
		list = addButton(new GuiList<>(1, x, y, 150, HEIGHT - 22, 16, translate("gui.dimstack.repl.list"), layers));
		addButton(new GuiButtonExt(2, x, y + HEIGHT - 20, 74, 20, translate("gui.dimstack.new")));
		rem = addButton(new GuiButtonExt(3, x + 76, y + HEIGHT - 20, 74, 20, translate("gui.dimstack.rem")));
		
		GuiLabel l;
		labelList.add(l = new GuiLabel(fontRenderer, 0, x += 158, y, 150, 10, 0x7f7f7f));
		l.addLine(translate("gui.dimstack.repl.new"));
		block = new GuiBlockSel(4, fontRenderer, complete, x, y += 10, 150, 20);
		block.setGuiResponder(this);
		textFields.add(block);
		labelList.add(l = new GuiLabel(fontRenderer, 1, x, y += 24, 150, 10, 0x7f7f7f));
		l.addLine(translate("gui.dimstack.ore.repl"));
		old = new GuiBlockSel(5, fontRenderer, complete, x, y += 10, 150, 20).enableList();
		old.setGuiResponder(this);
		textFields.add(old);
		
		y += 32;
		textFields.add(minY = new GuiTextField(6, fontRenderer, x + 75-30, y, 30, 20));
		minY.setGuiResponder(this);
		textFields.add(maxY = new GuiTextField(7, fontRenderer, x + 150-30, y, 30, 20));
		maxY.setGuiResponder(this);
		
		select(-1);
	}

	private void select(int i) {
		list.sel = i;
		Replacement e = getSel();
		boolean enable = e != null;
		for (int j = labelList.size() - 1; j >= 2; j--)
			labelList.remove(j);
		minY.setVisible(false);
		maxY.setVisible(false);
		if (enable) {
			block.setText(BlockPredicate.serialize(e.repl));
			{String s = e.target.toString();
			if (s.startsWith("[") && s.endsWith("]")) s = s.substring(1, s.length() - 1);
			old.setText(s);}
			
			GuiLabel l;
			labelList.add(l = new GuiLabel(fontRenderer, 2, minY.x - 45, minY.y, 45, 20, 0xffffff).setCentered());
			for (String s : translate("gui.dimstack.layer.bot").split("\n", 2)) l.addLine(s);
			minY.setText(Integer.toString(e.minY));
			minY.setVisible(true);
			labelList.add(l = new GuiLabel(fontRenderer, 3, maxY.x - 45, maxY.y, 45, 20, 0xffffff).setCentered());
			for (String s : translate("gui.dimstack.layer.top").split("\n", 2)) l.addLine(s);
			maxY.setText(Integer.toString(e.maxY));
			maxY.setVisible(true);
		} else {
			block.setText("");
			old.setText("");
		}
		block.setEnabled(enable);
		old.setEnabled(enable);
		minY.setEnabled(minY.getVisible());
		maxY.setEnabled(maxY.getVisible());
		rem.enabled = enable;
	}

	private Replacement getSel() {
		return list.sel >= 0 && list.sel < list.list.size() ? list.list.get(list.sel).entry : null;
	}

	@Override
	public void drawScreen(int mx, int my, float t) {
		super.drawScreen(mx, my, t);
		block.drawOverlay();
		old.drawOverlay();
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		int s = list.sel;
		switch(b.id) {
		case 1:
			select(list.sel);
			break;
		case 2:
			if (++s < 0 || s > list.list.size()) s = list.list.size();
			Replacement r = new Replacement(new BlockPredicate(), Blocks.AIR.getDefaultState(), 0, 0);
			cfg.replacements.add(r);
			list.list.add(s, new Repl(r));
			select(s);
			break;
		case 3:
			if (s >= 0 && s < list.list.size()) {
				cfg.replacements.remove(list.list.remove(s).entry);
				select(-1);
			}
			break;
		default: super.actionPerformed(b);
		}
	}

	@Override
	public void setEntryValue(int id, boolean value) {
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
		Replacement e = getSel();
		if (e == null) return;
		switch(id) {
		case 4:
			e.repl = BlockPredicate.parse(value);
			break;
		case 5:
			e.target = new BlockPredicate(value.split(","));
			break;
		case 6:
			try {
				e.minY = Integer.parseInt(value);
			} catch (NumberFormatException ex) {}
			break;
		case 7:
			try {
				e.maxY = Integer.parseInt(value);
			} catch (NumberFormatException ex) {}
			break;
		}
	}

	static class Repl implements IDrawableEntry {

		final Replacement entry;

		Repl(Replacement entry) {
			this.entry = entry;
		}

		@Override
		public void draw(Minecraft mc, int x, int y, int w, int h, float t) {
			RenderUtil.drawPortrait(entry.repl, x + w - h, y, 0, h);
			String s = format("gui.dimstack.repl.e", entry.minY, entry.maxY);
			w -= h + 1 + mc.fontRenderer.getStringWidth(s);
			mc.fontRenderer.drawString(s, x + w, y + (h - mc.fontRenderer.FONT_HEIGHT) / 2, 0xffffff00);
			s = entry.target.toString();
			s = s.substring(1, s.length() - 1);
			for (String s1 : s.split(",")) {
				if ((w-=h) < 4) return;
				RenderUtil.drawPortrait(BlockPredicate.parse(s1.trim()), x + 2, y, 0, h);
				x += h;
			}
		}

	}

}
