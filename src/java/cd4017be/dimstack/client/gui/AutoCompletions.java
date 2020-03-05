package cd4017be.dimstack.client.gui;

import net.minecraft.world.biome.Biome;

/**
 * @author CD4017BE
 *
 */
public class AutoCompletions {

	private static BlockStateCompletion BLOCKSTATES;
	private static AutoCompletion<Biome> BIOMES;

	public static BlockStateCompletion blockstates() {
		if (BLOCKSTATES != null && BLOCKSTATES.isAlive()) return BLOCKSTATES;
		BLOCKSTATES = new BlockStateCompletion(12);
		BLOCKSTATES.start();
		return BLOCKSTATES;
	}

	public static AutoCompletion<Biome> biomes() {
		if (BIOMES != null && BIOMES.isAlive()) return BIOMES;
		BIOMES = new AutoCompletion<>(12, Biome.REGISTRY);
		BIOMES.start();
		return BIOMES;
	}

}
