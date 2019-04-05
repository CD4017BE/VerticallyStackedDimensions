package cd4017be.dimstack.api.util;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.oredict.OreDictionary;


/**
 * @author CD4017BE
 *
 */
public class BlockPredicate implements Predicate<IBlockState> {

	static final Random RAND = new Random();
	final Predicate<IBlockState>[] matchers;
	final String[] cfg;

	@SuppressWarnings("unchecked")
	public BlockPredicate(String... blocks) {
		int n = blocks.length;
		this.cfg = blocks;
		this.matchers = new Predicate[n];
		for (int i = 0; i < n; i++) {
			String s = cfg[i];
			if (s.startsWith("ore:")) {
				int id = OreDictionary.getOreID(s.substring(4));
				matchers[i] = (state) -> {
					Block block = state.getBlock();
					ItemStack stack = new ItemStack(block.getItemDropped(state, RAND, 0), block.damageDropped(state));
					if (!stack.isEmpty())
						for (int o : OreDictionary.getOreIDs(stack))
							if (o == id) return true;
					return false;
				};
			} else if (s.indexOf('@') >= 0)
				matchers[i] = Predicate.isEqual(parse(s));
			else {
				Block block = Block.getBlockFromName(s);
				matchers[i] = (state)-> state.getBlock() == block;
			}
		}
	}

	public static BlockPredicate loadNBT(NBTTagList list) {
		int n = list.tagCount();
		String[] cfg = new String[n];
		for (int i = 0; i < n; i++)
			cfg[i] = list.getStringTagAt(i);
		return new BlockPredicate(cfg);
	}

	public NBTTagList writeNBT() {
		NBTTagList list = new NBTTagList();
		for (String s : cfg)
			list.appendTag(new NBTTagString(s));
		return list;
	}

	@Override
	public boolean test(IBlockState arg0) {
		for (Predicate<IBlockState> p : matchers)
			if (p.test(arg0))
				return true;
		return matchers.length == 0;
	}

	public static BlockPredicate parse(Object param) {
		if (param instanceof String)
			return new BlockPredicate((String)param);
		if (param instanceof Object[]) {
			Object[] arr = (Object[])param;
			return new BlockPredicate(Arrays.copyOf(arr, arr.length, String[].class));
		} else if (param == null)
			return new BlockPredicate();
		else
			throw new IllegalArgumentException("expected String or Array of Strings");
	}

	@SuppressWarnings("deprecation")
	public static IBlockState parse(String s) {
		try {
			if (s.startsWith("ore:")) {
				for (ItemStack stack : OreDictionary.getOres(s.substring(4))) {
					Item item = stack.getItem();
					if (!(item instanceof ItemBlock)) continue;
					return ((ItemBlock)item).getBlock().getStateFromMeta(item.getMetadata(stack.getMetadata()));
				}
				return Blocks.AIR.getDefaultState();
			}
			int p = s.indexOf('@');
			if (p < 0) return Block.getBlockFromName(s).getDefaultState();
			Block block = Block.getBlockFromName(s.substring(0, p));
			return block.getStateFromMeta(Integer.parseInt(s.substring(p + 1)));
		} catch (Exception e) {
			return Blocks.AIR.getDefaultState();
		}
	}

	public static String serialize(IBlockState s) {
		Block b = s.getBlock();
		return b.getRegistryName() + "@" + b.getMetaFromState(s);
	}

	/**
	 * make the given chunk primer ignore placement calls for the given blockstate<br>
	 * Note: you can only have one block disabled at a time this way so choose wisely ...
	 * @param cp the chunk primer to configure
	 * @param state the state to disable or null to disable none
	 */
	public static void disableBlock(ChunkPrimer cp, IBlockState state) {
		//cp.ingnoredBlock = state; //implemented via ASM
	}

}
