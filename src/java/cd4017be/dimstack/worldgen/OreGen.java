package cd4017be.dimstack.worldgen;

import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.cfg.DisableVanillaOres;
import cd4017be.dimstack.core.PortalConfiguration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author cd4017be
 */
public class OreGen {

	static boolean registered;

	public static void register() {
		if (registered) return;
		registered = true;
		MinecraftForge.ORE_GEN_BUS.register(Main.proxy.worldgenOres);
	}

	@SubscribeEvent
	public void onGenerate(OreGenEvent.GenerateMinable event) {
		DisableVanillaOres cfg = PortalConfiguration.get(event.getWorld()).getSettings(DisableVanillaOres.class, false);
		if (cfg == null) return;
		if (cfg.disabled(event.getType()))
			event.setResult(Result.DENY);
	}

	public void initConfig(ConfigConstants cfg) {
		DisableVanillaOres d = PortalConfiguration.get(0).getSettings(DisableVanillaOres.class, true);
		for (EventType t : EventType.values())
			if (cfg.get("disable_" + t.name().toLowerCase(), Boolean.class, Boolean.FALSE))
				d.disable(t);
		if (d.disabled(EventType.QUARTZ))
			PortalConfiguration.get(-1).getSettings(DisableVanillaOres.class, true).disable(EventType.QUARTZ);
	}

}
