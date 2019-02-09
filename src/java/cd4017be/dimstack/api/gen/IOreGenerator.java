package cd4017be.dimstack.api.gen;

import java.util.Random;

import cd4017be.dimstack.api.util.ICfgListEntry;
import net.minecraft.world.chunk.Chunk;


/**
 * IOreGenerators run during chunk population at default priority (0).
 * @author CD4017BE
 */
public interface IOreGenerator extends ICfgListEntry {

	/**
	 * called during chunk population to generate ores (or other things) in the world
	 * @param chunk world and chunk position to generate for
	 * @param rand world gen random
	 */
	public abstract void generate(Chunk chunk, Random rand);

}
