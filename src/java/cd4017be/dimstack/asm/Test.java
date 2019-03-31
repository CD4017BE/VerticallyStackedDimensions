package cd4017be.dimstack.asm;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.DisabledPortals;
import cd4017be.dimstack.api.util.BlockPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * Test case for {@link ChunkPrimerTransformer}
 * @author CD4017BE
 */
public class Test {

	public static void run() {
		Main.LOG.info("Testing ClassTransformer result of {} for correct functionality ...", ChunkPrimerTransformer.class);
		try {
			dryTest();
			Main.LOG.info("dryTest passed!");
		} catch(Throwable e) {
			Main.LOG.fatal("dryTest failed! Please report this to the mod author!", e);
		}
		try {
			mainTest();
			Main.LOG.info("mainTest passed!");
		} catch(Throwable e) {
			Main.LOG.fatal("mainTest failed! Please report this to the mod author!", e);
		}
		Main.LOG.info("Testing ClassTransformer result of {} for correct functionality ...", BlockPortalTransformer.class);
		try {
			portalTest();
			Main.LOG.info("portalTest passed!");
		} catch(Throwable e) {
			Main.LOG.fatal("portalTest failed! Please report this to the mod author!", e);
		}
	}

	private static void dryTest() throws Exception {
		ChunkPrimer cp = new ChunkPrimer();
		IBlockState in = Blocks.STONE.getDefaultState(); 
		cp.setBlockState(8, 25, 8, in);
		assEq(in, cp.getBlockState(8, 25, 8), "Non blacklisted block was not placed:");
		cp.setBlockState(8, 25, 8, null);
		assEq(Blocks.AIR.getDefaultState(), cp.getBlockState(8, 25, 8), "Default block was not placed:");
	}

	private static void mainTest() throws Exception {
		ChunkPrimer cp = new ChunkPrimer();
		IBlockState in = Blocks.STONE.getDefaultState(), bl = Blocks.OBSIDIAN.getDefaultState();
		BlockPredicate.disableBlock(cp, bl);
		cp.setBlockState(8, 25, 8, in);
		assEq(in, cp.getBlockState(8, 25, 8), "Non blacklisted block was not placed:");
		cp.setBlockState(8, 25, 8, bl);
		assEq(in, cp.getBlockState(8, 25, 8), "Blacklisted block was placed:");
		cp.setBlockState(8, 25, 8, null);
		assEq(Blocks.AIR.getDefaultState(), cp.getBlockState(8, 25, 8), "Default block was not placed:");
	}

	private static void portalTest() throws Exception {
		final String key = "nether";
		DisabledPortals cfg = Main.dimstack.getSettings(DisabledPortals.class, true);
		boolean disabled = cfg.contains(key);
		try {
			cfg.remove(key);
			try {
				Blocks.PORTAL.trySpawnPortal(null, null);
				throw new Exception("BlockPortal didn't try to place portal although nether portal enabled!");
			} catch (NullPointerException e) {}
			
			cfg.add(key);
			try {
				Blocks.PORTAL.trySpawnPortal(null, null);
			} catch (NullPointerException e) {
				throw new Exception("BlockPortal tried to place portal although nether portal disabled!", e);
			}
		} finally {
			if (disabled) cfg.add(key);
			else cfg.remove(key);
		}
	}

	private static void assEq(IBlockState exp, IBlockState got, String message) throws Exception {
		if (exp != got)
			throw new Exception(message + " expected " + BlockPredicate.serialize(exp) + " but got " + BlockPredicate.serialize(got) + ".");
	}

}
