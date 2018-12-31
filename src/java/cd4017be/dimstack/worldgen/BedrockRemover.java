package cd4017be.dimstack.worldgen;

import java.util.Random;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.dimstack.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 
 * @author cd4017be
 */
public class BedrockRemover implements IWorldGenerator, IRecipeHandler {

	private static final String BEDROCK_REPL = "bedrockRepl";

	public BedrockRemover() {
		RecipeAPI.Handlers.put(BEDROCK_REPL, this);
		//mods commonly use 0 for their ore-gen, so this runs just before.
		GameRegistry.registerWorldGenerator(this, -1);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		PortalConfiguration pc = PortalConfiguration.get(world);
		if (pc.LBedrockRepl != null)
			removeBedrock(world, chunkX, chunkZ, pc.LBedrockMin, pc.LBedrockMax, pc.LBedrockRepl);
		if (pc.UBedrockRepl != null)
			removeBedrock(world, chunkX, chunkZ, pc.UBedrockMin, pc.UBedrockMax, pc.UBedrockRepl);
	}

	private void removeBedrock(World world, int cx, int cz, int y0, int y1, IBlockState repl) {
		MutableBlockPos pos = new MutableBlockPos();
		int x0 = cx << 4, x1 = x0 + 16;
		for (int z = cz << 4, z1 = z + 16; z < z1; z++)
			for (int x = x0; x < x1; x++)
				for (int y = y0; y < y1; y++)
					if (world.getBlockState(pos.setPos(x, y, z)).getBlock() == Blocks.BEDROCK)
						world.setBlockState(pos, repl, 16);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addRecipe(Parameters param) {
		ItemStack is = param.get(2, ItemStack.class);
		Item i = is.getItem();
		if (!(i instanceof ItemBlock)) throw new IllegalArgumentException("supplied item has no registered block equivalent");
		double[] vec = param.getVector(3);
		if (vec.length != 2) throw new IllegalArgumentException("height parameter must have 2 elements");
		IBlockState repl = ((ItemBlock)i).getBlock().getStateFromMeta(i.getMetadata(is.getMetadata()));
		PortalConfiguration pc = PortalConfiguration.get((int) param.getNumber(1));
		if (pc.LBedrockRepl == null) {
			pc.LBedrockRepl = repl;
			pc.LBedrockMin = (int) vec[0];
			pc.LBedrockMax = (int) vec[1];
		} else if (pc.UBedrockRepl == null) {
			pc.UBedrockRepl = repl;
			pc.UBedrockMin = (int) vec[0];
			pc.UBedrockMax = (int) vec[1];
		} else throw new IllegalArgumentException("Both bedrock replacements for dim " + pc.dimId + " already defined!");
	}

}
