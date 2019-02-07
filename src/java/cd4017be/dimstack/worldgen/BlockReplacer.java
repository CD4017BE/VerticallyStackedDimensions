package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.dimstack.cfg.BlockPredicate;
import cd4017be.dimstack.cfg.BlockReplacements;
import cd4017be.dimstack.cfg.BlockReplacements.Replacement;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 
 * @author cd4017be
 */
public class BlockReplacer implements IWorldGenerator, IRecipeHandler {

	private static final String BEDROCK_REPL = "bedrockRepl", BLOCK_REPL = "blockRepl";

	public BlockReplacer() {
		RecipeAPI.Handlers.put(BEDROCK_REPL, this);
		RecipeAPI.Handlers.put(BLOCK_REPL, this);
		//mods commonly use 0 for their ore-gen, so this runs just before.
		GameRegistry.registerWorldGenerator(this, -1);
	}

	@Override
	public void addRecipe(Parameters param) {
		String key = param.getString(0);
		int n = 2;
		BlockPredicate target;
		if (key.equals(BEDROCK_REPL)) target = new BlockPredicate(Blocks.BEDROCK.getRegistryName().toString());
		else target = BlockPredicate.parse(param.get(n++));
		IBlockState repl = BlockPredicate.parse(param.get(n++, ItemStack.class));
		double[] vec = param.getVector(n);
		if (vec.length != 2) throw new IllegalArgumentException("height parameter must have 2 elements");
		Replacement r = new Replacement(target, repl, (int)vec[0], (int)vec[1]);
		PortalConfiguration.get((int)param.getNumber(1))
			.getSettings(BlockReplacements.class, true)
			.replacements.add(r);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		BlockReplacements repl = PortalConfiguration.get(world).getSettings(BlockReplacements.class, false);
		if (repl != null)
			for (Replacement r : repl.replacements)
				r.doReplace(world, chunkX, chunkZ);
	}

}
