package cd4017be.dimstack.client.gui;

import static cd4017be.lib.util.TooltipUtil.format;
import static cd4017be.lib.util.TooltipUtil.translate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.OreGeneration;
import cd4017be.dimstack.api.gen.IOreGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.client.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.config.GuiButtonExt;

/**
 * @author CD4017BE
 *
 */
public class GuiCustomOres extends GuiMenuBase implements GuiResponder {

	private static final int WIDTH = 308, HEIGHT = 7*24 - 4 + 20;
	private final OreGeneration cfg;
	private BlockStateCompletion complete;
	private GuiBlockSel block, rock;
	private GuiTextField maxY, minY, mainY;
	private GuiButton mode, rem;
	private GuiSlider bpv, vpc;
	private GuiList<Ore> list;

	public GuiCustomOres(GuiScreen parent, IDimension dim) {
		super(parent);
		cfg = dim.getSettings(OreGeneration.class, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.complete = AutoCompletions.blockstates();
		title = translate("gui.dimstack.newOre");
		int x = (width - WIDTH) / 2, y = (height - HEIGHT) / 2;
		
		List<Ore> layers;
		if (list != null) layers = list.list;
		else {
			layers = new ArrayList<>();
			for (IOreGenerator gen : cfg.entries) {
				NBTTagCompound nbt = gen.writeNBT();
				nbt.setString("id", gen.getRegistryName());
				layers.add(new Ore(nbt));
			}
		}
		list = addButton(new GuiList<>(1, x, y, 150, HEIGHT - 22, 16, translate("gui.dimstack.ore.list"), layers));
		addButton(new GuiButtonExt(2, x, y + HEIGHT - 20, 74, 20, translate("gui.dimstack.new")));
		rem = addButton(new GuiButtonExt(3, x + 76, y + HEIGHT - 20, 74, 20, translate("gui.dimstack.rem")));
		
		GuiLabel l;
		labelList.add(l = new GuiLabel(fontRenderer, 0, x += 158, y, 150, 10, 0x7f7f7f));
		l.addLine(translate("gui.dimstack.ore.block"));
		block = new GuiBlockSel(4, fontRenderer, complete, x, y += 10, 150, 20);
		block.setGuiResponder(this);
		textFields.add(block);
		labelList.add(l = new GuiLabel(fontRenderer, 1, x, y += 24, 150, 10, 0x7f7f7f));
		l.addLine(translate("gui.dimstack.ore.repl"));
		rock = new GuiBlockSel(5, fontRenderer, complete, x, y += 10, 150, 20).enableList();
		rock.setGuiResponder(this);
		textFields.add(rock);
		
		bpv = addButton(new GuiSlider(this, 6, x, y += 24, translate("gui.dimstack.ore.bpv"), 1, 128, 0, (id, name, val)-> String.format(name.startsWith("Format error: ") ? name.substring(14) : name, val)));
		bpv.setWidth(150);
		vpc = addButton(new GuiSlider(this, 7, x, y += 24, translate("gui.dimstack.ore.vpc"), 1F/3F, 48, 0, (id, name, val)-> String.format(name.startsWith("Format error: ") ? name.substring(14) : name, val)));
		vpc.setWidth(150);
		
		mode = addButton(new GuiButtonExt(8, x, y += 24, 150, 20, ""));
		y += 24;
		textFields.add(minY = new GuiTextField(9, fontRenderer, x + 75-30, y, 30, 20));
		minY.setGuiResponder(this);
		textFields.add(maxY = new GuiTextField(10, fontRenderer, x + 150-30, y, 30, 20));
		maxY.setGuiResponder(this);
		y += 24;
		textFields.add(mainY = new GuiTextField(11, fontRenderer, x + 150-30, y, 30, 20));
		mainY.setGuiResponder(this);
		
		select(-1);
	}

	private void select(int i) {
		list.sel = i;
		NBTTagCompound nbt = getSel();
		boolean enable = nbt != null;
		for (int j = labelList.size() - 1; j >= 2; j--)
			labelList.remove(j);
		minY.setVisible(false);
		maxY.setVisible(false);
		mainY.setVisible(false);
		if (enable) {
			vpc.setSliderValue(nbt.getFloat("vpc"), false);
			bpv.setSliderValue(nbt.getShort("bpv"), false);
			block.setText(nbt.getString("ore"));
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (NBTBase tag : nbt.getTagList("target", NBT.TAG_STRING)) {
				if (first) first = false;
				else sb.append(", ");
				sb.append(((NBTTagString)tag).getString());
			}
			rock.setText(sb.toString());
			
			String id = nbt.getString("id");
			mode.displayString = translate("dimstack.ore." + id);
			GuiLabel l;
			if (id.equals("even") || id.equals("center")) {
				labelList.add(l = new GuiLabel(fontRenderer, 2, minY.x - 45, minY.y, 45, 20, 0xffffff).setCentered());
				for (String s : translate("gui.dimstack.layer.bot").split("\n", 2)) l.addLine(s);
				minY.setText(Integer.toString(nbt.getShort("minY")));
				minY.setVisible(true);
				labelList.add(l = new GuiLabel(fontRenderer, 3, maxY.x - 45, maxY.y, 45, 20, 0xffffff).setCentered());
				for (String s : translate("gui.dimstack.layer.top").split("\n", 2)) l.addLine(s);
				maxY.setText(Integer.toString(nbt.getShort("maxY")));
				maxY.setVisible(true);
				if (id.equals("center")) {
					labelList.add(l = new GuiLabel(fontRenderer, 4, mainY.x - 120, mainY.y, 120, 20, 0xffffff).setCentered());
					for (String s : translate("gui.dimstack.ore.peak").split("\n", 2)) l.addLine(s);
					mainY.setText(Integer.toString(nbt.getShort("mainY")));
					mainY.setVisible(true);
				}
			} else if (id.equals("gauss")) {
				labelList.add(l = new GuiLabel(fontRenderer, 2, maxY.x - 120, maxY.y, 120, 20, 0xffffff).setCentered());
				for (String s : translate("gui.dimstack.ore.main").split("\n", 2)) l.addLine(s);
				maxY.setText(Float.toString(nbt.getFloat("mainY")));
				maxY.setVisible(true);
				labelList.add(l = new GuiLabel(fontRenderer, 3, mainY.x - 120, mainY.y, 120, 20, 0xffffff).setCentered());
				for (String s : translate("gui.dimstack.ore.dev").split("\n", 2)) l.addLine(s);
				mainY.setText(Float.toString(nbt.getFloat("devY")));
				mainY.setVisible(true);
			} else {
				labelList.add(l = new GuiLabel(fontRenderer, 2, mode.x, mode.y + 24, 150, 20, 0xff4040).setCentered());
				l.addLine("Editing not supported,");
				l.addLine("better don't modify!");
			}
		} else {
			block.setText("");
			rock.setText("");
			mode.displayString = "";
		}
		block.setEnabled(enable);
		rock.setEnabled(enable);
		minY.setEnabled(minY.getVisible());
		maxY.setEnabled(maxY.getVisible());
		mainY.setEnabled(mainY.getVisible());
		mode.enabled = enable;
		vpc.enabled = enable;
		bpv.enabled = enable;
		rem.enabled = enable;
	}

	private NBTTagCompound getSel() {
		return list.sel >= 0 && list.sel < list.list.size() ? list.list.get(list.sel).nbt : null;
	}

	@Override
	public void drawScreen(int mx, int my, float t) {
		super.drawScreen(mx, my, t);
		block.drawOverlay();
		rock.drawOverlay();
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
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("id", "even");
			list.list.add(s, new Ore(nbt));
			select(s);
			break;
		case 3:
			if (s >= 0 && s < list.list.size()) {
				list.list.remove(s);
				select(-1);
			}
			break;
		case 8:
			if ((nbt = getSel()) != null) {
				String id = nbt.getString("id");
				if (id.equals("even")) id = "center";
				else if (id.equals("center")) id = "gauss";
				else if (id.equals("gauss")) id = "even";
				else break;
				nbt.setString("id", id);
				select(s);
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
		NBTTagCompound nbt = getSel();
		if (nbt == null) return;
		switch(id) {
		case 6:
			nbt.setShort("bpv", (short)Math.round(value));
			break;
		case 7:
			nbt.setFloat("vpc", value);
			break;
		}
	}

	@Override
	public void setEntryValue(int id, String value) {
		NBTTagCompound nbt = getSel();
		if (nbt == null) return;
		String m = nbt.getString("id");
		switch(id) {
		case 4:
			nbt.setString("ore", value);
			break;
		case 5:
			NBTTagList list = new NBTTagList();
			if (!value.isEmpty())
				for (String e : value.split(","))
					list.appendTag(new NBTTagString(e.trim()));
			nbt.setTag("target", list);
			break;
		case 9:
			try {
				if (m.equals("even") || m.equals("center"))
					nbt.setShort("minY", (short)(Integer.parseInt(value)));
			} catch (NumberFormatException e) {}
			break;
		case 10:
			try {
				if (m.equals("even") || m.equals("center"))
					nbt.setShort("maxY", (short)(Integer.parseInt(value)));
				else if(m.equals("gauss"))
					nbt.setFloat("mainY", Float.parseFloat(value));
			} catch (NumberFormatException e) {}
			break;
		case 11:
			try {
				if (m.equals("center"))
					nbt.setShort("mainY", (short)(Integer.parseInt(value)));
				else if(m.equals("gauss"))
					nbt.setFloat("devY", Float.parseFloat(value));
			} catch (NumberFormatException e) {}
			break;
		}
	}

	@Override
	public void onGuiClosed() {
		NBTTagList tl = new NBTTagList();
		for (Ore l : list.list)
			tl.appendTag(l.nbt);
		cfg.deserializeNBT(tl);
		super.onGuiClosed();
	}

	static class Ore implements IDrawableEntry {

		final NBTTagCompound nbt;

		Ore(NBTTagCompound nbt) {
			this.nbt = nbt;
		}

		@Override
		public void draw(Minecraft mc, int x, int y, int w, int h, float t) {
			String s = format("dimstack.ore._" + nbt.getString("id"), nbt.getShort("minY"), nbt.getShort("maxY"), nbt.getFloat("mainY"), nbt.getFloat("devY"));
			mc.fontRenderer.drawString(s, x + (h + 20 + w - mc.fontRenderer.getStringWidth(s)) / 2, y + (h - mc.fontRenderer.FONT_HEIGHT) / 2, 0xffffff00);
			String block = nbt.getString("ore");
			RenderUtil.drawPortrait(BlockPredicate.parse(block), x + 22, y, 0, h);
			s = format("\\%0.2u", Math.rint(nbt.getFloat("vpc") * (float)nbt.getShort("bpv") * 10.0) / 10.0);
			mc.fontRenderer.drawString(s, x + 22 - mc.fontRenderer.getStringWidth(s), y + (h - mc.fontRenderer.FONT_HEIGHT) / 2, 0x80ff80);
		}

	}

}
