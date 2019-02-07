package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.Main;
import cd4017be.dimstack.cfg.BlockPredicate;
import cd4017be.dimstack.cfg.DisableVanillaOres;
import cd4017be.dimstack.cfg.OreGeneration;
import cd4017be.dimstack.cfg.OreGeneration.OreGenEven;
import cd4017be.dimstack.cfg.OreGeneration.OreGenGaussian;
import cd4017be.dimstack.cfg.OreGeneration.OreGenCentered;
import cd4017be.dimstack.cfg.OreGeneration.OreGenerator;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author cd4017be
 */
public class OreGen implements IWorldGenerator, IRecipeHandler {

	private static final String OREGEN = "oregen";
	static boolean registered;

	public static void register() {
		if (registered) return;
		registered = true;
		MinecraftForge.ORE_GEN_BUS.register(Main.proxy.worldgenOres);
	}

	public OreGen() {
		GameRegistry.registerWorldGenerator(this, 0);
		RecipeAPI.Handlers.put(OREGEN, this);
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

	@Override
	public void generate(Random random, int cx, int cy, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		OreGeneration cfg = PortalConfiguration.get(world).getSettings(OreGeneration.class, false);
		if (cfg == null) return;
		Chunk chunk = chunkProvider.provideChunk(cx, cy);
		for (OreGenerator og : cfg.generators)
			og.generate(chunk, random);
	}

	@Override
	public void addRecipe(Parameters param) {
		OreGeneration cfg = PortalConfiguration.get(param.getIndex(1)).getSettings(OreGeneration.class, true);
		BlockPredicate target = BlockPredicate.parse(param.get(2));
		float veins = (float)param.getNumber(3);
		ItemStack stack = param.get(4, ItemStack.class);
		IBlockState ore = BlockPredicate.parse(stack);
		double[] heights = param.getVectorOrAll(6);
		
		switch(OreGeneration.getType(param.getString(5))) {
		case OreGenEven.ID:
			cfg.generators.add(
				new OreGenEven(ore, stack.getCount(), veins, target, (int)heights[0], (int)heights[1])
			); break;
		case OreGenCentered.ID:
			cfg.generators.add(
				new OreGenCentered(ore, stack.getCount(), veins, target, (int)heights[0], (int)heights[1], (int)heights[2])
			); break;
		case OreGenGaussian.ID:
			cfg.generators.add(
				new OreGenGaussian(ore, stack.getCount(), veins, target, (float)heights[0], (float)heights[1])
			); break;
		}
	}

}
