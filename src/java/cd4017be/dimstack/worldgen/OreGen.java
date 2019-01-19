package cd4017be.dimstack.worldgen;

import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.core.IDimensionSettings;
import cd4017be.dimstack.core.PortalConfiguration;
import net.minecraft.nbt.NBTTagCompound;
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

	private static boolean registered;

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

	public static class DisableVanillaOres implements IDimensionSettings {

		private short disabled;

		public void reset() {
			disabled = 0;
		}

		public boolean disabled(EventType ore) {
			return (disabled & 1 << ore.ordinal()) != 0;
		}

		public void disable(EventType ore) {
			disabled |= 1 << ore.ordinal();
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			for (EventType t : EventType.values())
				if (disabled(t))
					nbt.setBoolean(t.name().toLowerCase(), true);
			checkReg();
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			reset();
			for (EventType t : EventType.values())
				if (nbt.getBoolean(t.name().toLowerCase()))
					disable(t);
			checkReg();
		}

		private void checkReg() {
			if (registered || disabled == 0) return;
			registered = true;
			MinecraftForge.ORE_GEN_BUS.register(Main.proxy.worldgenOres);
		}

	}

}
