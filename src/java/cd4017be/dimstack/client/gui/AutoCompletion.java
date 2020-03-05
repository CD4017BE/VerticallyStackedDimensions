package cd4017be.dimstack.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/** @author CD4017BE */
public class AutoCompletion<T> extends Thread {

	private static final int INT_CHECK = 1000;

	private final RegistryNamespaced<ResourceLocation, T> registry;
	private final ArrayList<String> domains = new ArrayList<>();
	private volatile boolean run;
	private volatile String search = "";
	protected final String[] results;
	protected int lastFilled;

	public AutoCompletion(int n, RegistryNamespaced<ResourceLocation, T> registry) {
		results = new String[n];
		this.registry = registry;
	}

	@Override
	public void run() {
		if(domains.isEmpty())
			for(ModContainer mod : Loader.instance().getActiveModList())
			domains.add(mod.getModId());
		run = true;
		String curSearch = "";
		while(run) {
			String ns = search;
			if(!ns.equals(curSearch)) {
				curSearch = ns;
				if(search(curSearch)) continue;
			}
			synchronized(this) {
				try {
					wait();
				} catch(InterruptedException e) {}
			}
		}
	}

	private boolean search(String s) {
		String[] results = this.results;
		Arrays.fill(results, 0, lastFilled, null);
		int n = 0, i = INT_CHECK;
		int p = s.indexOf(':');
		if(p < 0) for(String d : domains) {
			if(!d.startsWith(s)) continue;
			results[n++] = d + ":";
			if(interrupted()) {
				lastFilled = n;
				return true;
			}
			if(n == results.length) break;
		}
		else if(searchExtensions(s)) return false;
		else {
			String domain = s.substring(0, p);
			String path = s.substring(p + 1);
			for(ResourceLocation id : registry.getKeys()) {
				if(--i <= 0) {
					if(interrupted()) {
						lastFilled = n;
						return true;
					}
					i = INT_CHECK;
				}
				if(!id.getResourceDomain().equals(domain)) continue;
				if(!id.getResourcePath().startsWith(path)) continue;
				results[n++] = addExtension(id);
				if(n == results.length) break;
			}
		}
		lastFilled = n;
		return false;
	}

	protected String addExtension(ResourceLocation id) {
		return id.toString();
	}

	protected boolean searchExtensions(String s) {
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
