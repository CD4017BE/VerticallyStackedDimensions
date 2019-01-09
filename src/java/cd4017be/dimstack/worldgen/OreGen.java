package cd4017be.dimstack.worldgen;

import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author cd4017be
 */
public class OreGen {

	public boolean noCoal, noDiamond, noEmerald, noGold, noIron, noLapis, noRedstone, noQuartz;

	@SubscribeEvent
	public void onGenerate(OreGenEvent.GenerateMinable event) {
		int dim = event.getWorld().provider.getDimension();
		if (dim != 0 && dim != 1) return;
		switch(event.getType()) {
		case COAL: if (noCoal) break; else return;
		case DIAMOND: if (noDiamond) break; else return;
		case EMERALD: if (noEmerald) break; else return;
		case GOLD: if (noGold) break; else return;
		case IRON: if (noIron) break; else return;
		case LAPIS: if (noLapis) break; else return;
		case REDSTONE: if (noRedstone) break; else return;
		case QUARTZ: if (noQuartz) break; else return;
		default: return;
		}
		event.setResult(Result.DENY);
	}

	public void initConfig(ConfigConstants cfg) {
		boolean reg = false;
		reg |= noCoal = cfg.get("disable_coal", Boolean.class, noCoal);
		reg |= noDiamond = cfg.get("disable_diamond", Boolean.class, noDiamond);
		reg |= noEmerald = cfg.get("disable_emerald", Boolean.class, noEmerald);
		reg |= noGold = cfg.get("disable_gold", Boolean.class, noGold);
		reg |= noIron = cfg.get("disable_iron", Boolean.class, noIron);
		reg |= noLapis = cfg.get("disable_lapis", Boolean.class, noLapis);
		reg |= noRedstone = cfg.get("disable_redstone", Boolean.class, noRedstone);
		reg |= noQuartz = cfg.get("disable_quartz", Boolean.class, noQuartz);
		if (reg) MinecraftForge.ORE_GEN_BUS.register(this);
	}

}
