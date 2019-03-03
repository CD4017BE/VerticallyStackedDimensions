package cd4017be.dimstack.client.gui;

import java.io.IOException;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.TransitionInfo;
import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import static cd4017be.lib.util.TooltipUtil.*;

/**
 * @author CD4017BE
 *
 */
public class GuiTransition extends GuiMenuBase implements GuiResponder {

	private final TransitionInfo cfg;
	private BlockStateCompletion complete;
	private GuiBlockSel topBlock, botBlock;

	public GuiTransition(GuiScreen parent, IDimension dim) {
		super(parent);
		this.cfg = dim.getSettings(TransitionInfo.class, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.complete = BlockStateCompletion.get();
		title = translate("gui.dimstack.trans");
		int y = (height - 28 * 4 - 10 * 2 + 8) / 2, x = (width - 200) / 2;
		GuiLabel l = new GuiLabel(fontRenderer, 0, x, y, 200, 10, 0xffffff);
		l.addLine(translate("gui.dimstack.trans.topblock"));
		labelList.add(l);
		topBlock = new GuiBlockSel(1, fontRenderer, complete, x, y += 10, 200, 20);
		topBlock.setGuiResponder(this);
		if (cfg.blockTop != null) topBlock.setText(BlockPredicate.serialize(cfg.blockTop));
		textFields.add(topBlock);
		GuiSlider sl = addButton(new GuiSlider(this, 3, x, y += 28, translate("gui.dimstack.trans.topsize"), -2, 128, 0, (id, s, v)-> String.format("%s: %.0f", s, v)));
		sl.setWidth(200);
		sl.setSliderValue(cfg.sizeTop, false);
		
		l = new GuiLabel(fontRenderer, 0, x, y += 28, 200, 10, 0xffffff);
		l.addLine(translate("gui.dimstack.trans.botblock"));
		labelList.add(l);
		botBlock = new GuiBlockSel(2, fontRenderer, complete, x, y += 10, 200, 20);
		botBlock.setGuiResponder(this);
		if (cfg.blockBot != null) botBlock.setText(BlockPredicate.serialize(cfg.blockBot));
		textFields.add(botBlock);
		sl = addButton(new GuiSlider(this, 4, x, y += 28, translate("gui.dimstack.trans.botsize"), -2, 128, 0, (id, s, v)-> String.format("%s: %.0f", s, v)));
		sl.setWidth(200);
		sl.setSliderValue(cfg.sizeBot, false);
	}

	@Override
	public void drawScreen(int mx, int my, float t) {
		super.drawScreen(mx, my, t);
		topBlock.drawOverlay();
		botBlock.drawOverlay();
	}

	@Override
	public void onGuiClosed() {
		complete.dispose();
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		super.actionPerformed(b);
	}

	@Override
	public void setEntryValue(int id, boolean value) {
	}

	@Override
	public void setEntryValue(int id, float value) {
		switch(id) {
		case 3:
			cfg.sizeTop = Math.round(value);
			break;
		case 4:
			cfg.sizeBot = Math.round(value);
			break;
		}
	}

	@Override
	public void setEntryValue(int id, String value) {
		switch(id) {
		case 1:
			cfg.blockTop = value.isEmpty() ? null : BlockPredicate.parse(value);
			break;
		case 2:
			cfg.blockBot = value.isEmpty() ? null : BlockPredicate.parse(value);
			break;
		}
	}

}
