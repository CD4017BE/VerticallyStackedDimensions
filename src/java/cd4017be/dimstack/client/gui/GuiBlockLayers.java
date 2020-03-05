package cd4017be.dimstack.client.gui;

import static cd4017be.lib.util.TooltipUtil.translate;
import static cd4017be.lib.util.TooltipUtil.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.api.gen.ITerrainGenerator;
import cd4017be.dimstack.api.util.BlockPredicate;
import cd4017be.dimstack.client.RenderUtil;
import cd4017be.dimstack.worldgen.SimpleLayerGen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.client.config.GuiButtonExt;

/**
 * @author CD4017BE
 *
 */
public class GuiBlockLayers extends GuiMenuBase implements GuiResponder {

	private final TerrainGeneration cfg;
	private BlockStateCompletion complete;
	private GuiBlockSel block;
	private GuiTextField topY, botY;
	private GuiSlider topE, botE;
	private GuiList<Layer> list;
	private GuiButton rem;

	public GuiBlockLayers(GuiScreen parent, IDimension dim) {
		super(parent);
		cfg = dim.getSettings(TerrainGeneration.class, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.complete = AutoCompletions.blockstates();
		title = translate("gui.dimstack.layer");
		int h = 4*28 - 8, x = (width - 308) / 2, y = (height - h) / 2;
		
		List<Layer> layers;
		if (list != null) layers = list.list;
		else {
			layers = new ArrayList<>();
			for (ITerrainGenerator gen : cfg.entries) {
				NBTTagCompound nbt = gen.writeNBT();
				nbt.setString("id", gen.getRegistryName());
				layers.add(new Layer(nbt));
			}
		}
		list = addButton(new GuiList<>(1, x, y, 150, h - 22, 16, translate("gui.dimstack.layer.list"), layers));
		addButton(new GuiButtonExt(2, x, y + h - 20, 74, 20, translate("gui.dimstack.new")));
		rem = addButton(new GuiButtonExt(3, x + 76, y + h - 20, 74, 20, translate("gui.dimstack.rem")));
		
		block = new GuiBlockSel(4, fontRenderer, complete, x += 158, y, 150, 20);
		block.setGuiResponder(this);
		textFields.add(block);
		y += 28;
		GuiLabel l;
		labelList.add(l = new GuiLabel(fontRenderer, 0, x, y, 45, 20, 0xffffff).setCentered());
		for (String s : translate("gui.dimstack.layer.bot").split("\n", 2)) l.addLine(s);
		textFields.add(botY = new GuiTextField(5, fontRenderer, x + 75-30, y, 30, 20));
		botY.setGuiResponder(this);
		labelList.add(l = new GuiLabel(fontRenderer, 1, x + 75, y, 45, 20, 0xffffff).setCentered());
		for (String s : translate("gui.dimstack.layer.top").split("\n", 2)) l.addLine(s);
		textFields.add(topY = new GuiTextField(6, fontRenderer, x + 150-30, y, 30, 20));
		topY.setGuiResponder(this);
		topE = addButton(new GuiSlider(this, 8, x, y += 28, translate("gui.dimstack.layer.fadeUp"), 0, 64, 0, (id, name, val)-> String.format("%s: %.0f", name, val)));
		topE.setWidth(150);
		botE = addButton(new GuiSlider(this, 7, x, y += 28, translate("gui.dimstack.layer.fadeDown"), 0, 64, 0, (id, name, val)-> String.format("%s: %.0f", name, val)));
		botE.setWidth(150);
		
		select(-1);
	}

	private void select(int i) {
		list.sel = i;
		NBTTagCompound nbt = getSel();
		boolean enable = nbt != null && nbt.getString("id") == SimpleLayerGen.ID;
		if (enable) {
			block.setText(nbt.getString("block"));
			botY.setText(Integer.toString(nbt.getByte("minY") & 0xff));
			topY.setText(Integer.toString((nbt.getByte("maxY") & 0xff) + 1));
			botE.setSliderValue(nbt.getByte("extB") & 0xff, false);
			topE.setSliderValue(nbt.getByte("extT") & 0xff, false);
		} else {
			block.setText(nbt == null ? "" : "editor not yet implem.");
			topY.setText("");
			botY.setText("");
		}
		block.setEnabled(enable);
		botY.setEnabled(enable);
		topY.setEnabled(enable);
		botE.enabled = enable;
		topE.enabled = enable;
		rem.enabled = nbt != null;
	}

	private NBTTagCompound getSel() {
		return list.sel >= 0 && list.sel < list.list.size() ? list.list.get(list.sel).nbt : null;
	}

	@Override
	public void drawScreen(int mx, int my, float t) {
		super.drawScreen(mx, my, t);
		block.drawOverlay();
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
			nbt.setString("id", SimpleLayerGen.ID);
			list.list.add(s, new Layer(nbt));
			select(s);
			break;
		case 3:
			if (s >= 0 && s < list.list.size()) {
				list.list.remove(s);
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
		NBTTagCompound nbt = getSel();
		if (nbt == null) return;
		switch(id) {
		case 7:
			nbt.setByte("extB", (byte)Math.round(value));
			break;
		case 8:
			nbt.setByte("extT", (byte)Math.round(value));
			break;
		}
	}

	@Override
	public void setEntryValue(int id, String value) {
		NBTTagCompound nbt = getSel();
		if (nbt == null) return;
		switch(id) {
		case 4:
			nbt.setString("block", value);
			break;
		case 5:
			try {
				nbt.setByte("minY", (byte)(Integer.parseInt(value)));
			} catch (NumberFormatException e) {}
			break;
		case 6:
			try {
				nbt.setByte("maxY", (byte)(Integer.parseInt(value) - 1));
			} catch (NumberFormatException e) {}
			break;
		}
	}

	@Override
	public void onGuiClosed() {
		NBTTagList tl = new NBTTagList();
		for (Layer l : list.list)
			tl.appendTag(l.nbt);
		cfg.deserializeNBT(tl);
		super.onGuiClosed();
	}

	static class Layer implements IDrawableEntry {

		final NBTTagCompound nbt;

		Layer(NBTTagCompound nbt) {
			this.nbt = nbt;
		}

		@Override
		public void draw(Minecraft mc, int x, int y, int w, int h, float t) {
			int min = (nbt.getByte("minY") & 0xff) - (nbt.getByte("extB") & 0xff),
				max = (nbt.getByte("maxY") & 0xff) + (nbt.getByte("extT") & 0xff) + 1;
			mc.fontRenderer.drawString(format("dimstack.gen." + nbt.getString("id"), min, max), x + h + 2, y + (h - mc.fontRenderer.FONT_HEIGHT) / 2, 0xffffff00);
			String block = nbt.getString("block");
			RenderUtil.drawPortrait(BlockPredicate.parse(block), x + 1, y, 0, h);
		}

	}

}
