package cd4017be.dimstack.client.gui;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * @author CD4017BE
 *
 */
public class BlockStateCompletion extends Thread {

	private static final int INT_CHECK = 1000;
	private static BlockStateCompletion INSTANCE;

	private final ArrayList<String> domains = new ArrayList<>();
	private volatile boolean run;
	private volatile String search = "";
	private final String[] results;
	private int lastFilled;

	public static BlockStateCompletion get() {
		if (INSTANCE != null && INSTANCE.isAlive()) return INSTANCE;
		INSTANCE = new BlockStateCompletion(12);
		INSTANCE.start();
		return INSTANCE;
	}

	private BlockStateCompletion(int n) {
		results = new String[n];
	}

	@Override
	public void run() {
		if (domains.isEmpty())
			for (ModContainer mod : Loader.instance().getActiveModList())
				domains.add(mod.getModId());
		run = true;
		String curSearch = "";
		while(run) {
			String ns = search;
			if (!ns.equals(curSearch)) {
				curSearch = ns;
				if (search(curSearch)) continue;
			}
			synchronized (this) {
				try {wait();} catch (InterruptedException e) {}
			}
		}
	}

	private boolean search(String s) {
		String[] results = this.results;
		Arrays.fill(results, 0, lastFilled, null);
		int n = 0, i = INT_CHECK;
		int p = s.indexOf(':'), p1;
		if (p < 0) {
			for (String d : domains) {
				if (!d.startsWith(s)) continue;
				results[n++] = d + ":";
				if (interrupted()) {
					lastFilled = n;
					return true;
				}
				if (n == results.length) break;
			}
		} else if ((p1 = s.indexOf('@')) < 0){
			String domain = s.substring(0, p);
			String path = s.substring(p + 1);
			for (ResourceLocation id : Block.REGISTRY.getKeys()) {
				if (--i <= 0) {
					if (interrupted()) {
						lastFilled = n;
						return true;
					}
					i = INT_CHECK;
				}
				if (!id.getResourceDomain().equals(domain)) continue;
				if (!id.getResourcePath().startsWith(path)) continue;
				results[n++] = id.toString() + "@";
				if (n == results.length) break;
			}
		} else {
			String meta = s.substring(p1 + 1);
			s = s.substring(0, p1);
			Block block = Block.REGISTRY.getObject(new ResourceLocation(s));
			int states = 0;
			for (IBlockState state : block.getBlockState().getValidStates())
				states |= 1 << block.getMetaFromState(state);
			for (int j = 0; states != 0; j++, states >>>= 1) {
				if ((states & 1) == 0) continue;
				String m = Integer.toString(j);
				if (m.startsWith(meta)) {
					results[n++] = s + "@" + m;
					if (n == results.length) break;
				}
			}
		}
		lastFilled = n;
		return false;
	}

	public void dispose() {
		run = false;
		interrupt();
	}

	public void updateText(String t) {
		this.search = t;
		interrupt();
	}

	public String[] getResults() {
		return results;
	}

}
