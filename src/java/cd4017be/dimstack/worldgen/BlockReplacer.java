package cd4017be.dimstack.worldgen;

import java.util.ArrayList;
import java.util.Random;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.dimstack.core.IDimensionSettings;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.script.Parameters;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.util.Constants.NBT;
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

	@SuppressWarnings("deprecation")
	@Override
	public void addRecipe(Parameters param) {
		String key = param.getString(0);
		Replacement r = new Replacement();
		int n = 2;
		if (key.equals(BEDROCK_REPL)) r.target = Blocks.BEDROCK;
		else r.target = Block.getBlockFromName(param.getString(n++));
		ItemStack is = param.get(n++, ItemStack.class);
		Item i = is.getItem();
		if (!(i instanceof ItemBlock)) throw new IllegalArgumentException("supplied item has no registered block equivalent");
		r.repl = ((ItemBlock)i).getBlock().getStateFromMeta(i.getMetadata(is.getMetadata()));
		double[] vec = param.getVector(n);
		if (vec.length != 2) throw new IllegalArgumentException("height parameter must have 2 elements");
		r.minY = (int)vec[0];
		r.maxY = (int)vec[1];
		
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

	public static class BlockReplacements implements IDimensionSettings {

		ArrayList<Replacement> replacements = new ArrayList<>();

		@Override
		public NBTTagCompound serializeNBT() {
			if (replacements.isEmpty()) return null;
			NBTTagList list = new NBTTagList();
			for (Replacement r : replacements) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setShort("minY", (short)r.minY);
				tag.setShort("maxY", (short)r.maxY);
				tag.setString("target", r.target.getRegistryName().toString());
				Block block = r.repl.getBlock();
				tag.setString("block", block.getRegistryName().toString());
				tag.setByte("meta", (byte)block.getMetaFromState(r.repl));
				list.appendTag(tag);
			}
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setTag("entries", list);
			return nbt;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			NBTTagList list = nbt.getTagList("entries", NBT.TAG_COMPOUND);
			for (NBTBase tag : list) {
				NBTTagCompound ctag = (NBTTagCompound)tag;
				Replacement r = new Replacement();
				try {
					r.repl = Block.getBlockFromName(ctag.getString("block"))
							.getStateFromMeta(ctag.getByte("meta"));
				} catch (Exception e) {continue;}
				if ((r.target = Block.getBlockFromName(ctag.getString("target"))) == null)
					continue;
				r.minY = Math.max(0, ctag.getShort("minY"));
				r.maxY = Math.min(256, ctag.getShort("maxY"));
				replacements.add(r);
			}
		}

	}

	public static class Replacement {

		Block target;
		IBlockState repl;
		int minY, maxY;

		void doReplace(World world, int cx, int cz) {
			Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
			MutableBlockPos pos = new MutableBlockPos();
			Block target = this.target;
			IBlockState repl = this.repl;
			int x0 = cx << 4, x1 = x0 + 16,
				y0 = this.minY, y1 = this.maxY;
			for (int z = cz << 4, z1 = z + 16; z < z1; z++)
				for (int x = x0; x < x1; x++)
					for (int y = y0; y < y1; y++)
						if (chunk.getBlockState(pos.setPos(x, y, z)).getBlock() == target)
							chunk.setBlockState(pos, repl);
		}

	}

}
