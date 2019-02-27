package cd4017be.dimstack.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import cd4017be.lib.util.TooltipUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * @author CD4017BE
 */
@SideOnly(Side.CLIENT)
public class GuiMenuBase extends GuiScreen {
	static final boolean DEBUG = true;

	private final GuiScreen parent;
	protected GuiButton b_close;
	protected String title;
	protected List<GuiTextField> textFields = Lists.newArrayList();

	public GuiMenuBase(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void initGui() {
		labelList.clear();
		textFields.clear();
		b_close = addButton(new GuiButton(0, width / 2 - 75, height - 28, 150, 20, TooltipUtil.translate("gui.done")));
	}

	@Override
	public void updateScreen() {
		for (GuiTextField tf : textFields)
			tf.updateCursorCounter();
	}

	@Override
	protected void actionPerformed(GuiButton b) throws IOException {
		if (b == b_close) mc.displayGuiScreen(parent);
	}

	@Override
	protected void keyTyped(char c, int k) throws IOException {
		for (GuiTextField tf : textFields)
			if (tf.isFocused()) {
				tf.textboxKeyTyped(c, k);
				return;
			}
		if (DEBUG && k == Keyboard.KEY_F1) mc.displayGuiScreen(this);
		else if (k == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
	}

	@Override
	public void handleMouseInput() throws IOException {
		int z = Mouse.getEventDWheel();
		if (z != 0) {
			if (z > 1) z = 1;
			else if (z < -1) z = -1;
			int x = Mouse.getEventX() * width / mc.displayWidth;
			int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
			for (GuiButton b : buttonList)
				if (b instanceof IScrollInputHandler)
					((IScrollInputHandler)b).onScroll(mc, x, y, z);
		}
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mx, int my, int mb) throws IOException {
		super.mouseClicked(mx, my, mb);
		for (GuiTextField tf : textFields)
			tf.mouseClicked(mx, my, mb);
	}

	public void drawScreen(int mx, int my, float t) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, title, width / 2, 15, 0xffffff);
		for (GuiTextField tf : textFields)
			tf.drawTextBox();
		super.drawScreen(mx, my, t);
	}

}
