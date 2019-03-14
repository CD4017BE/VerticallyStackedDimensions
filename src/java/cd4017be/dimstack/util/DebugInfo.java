package cd4017be.dimstack.util;

import java.util.Random;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.api.IDimension;
import cd4017be.dimstack.api.IDimensionSettings;
import cd4017be.dimstack.api.TerrainGeneration;
import cd4017be.dimstack.core.Dimensionstack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.Context;

/**
 * 
 * @author cd4017be
 */
public class DebugInfo implements IDimensionSettings {

	private boolean chunksGenerated = false;
	private boolean initialized = false;
	private boolean logged = false;

	@Override
	public NBTBase serializeNBT() {
		return new NBTTagByte((byte) (chunksGenerated ? 1 : 0));
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		byte state = ((NBTPrimitive)nbt).getByte();
		chunksGenerated = (state & 1) != 0;
	}

	public void setGenerated() {
		if (!chunksGenerated) {
			chunksGenerated = true;
			Dimensionstack.markDirty();
		}
	}

	public void setInitialized() {
		initialized = true;
	}

	public void genTerrainLate(IDimension dim, IChunkGenerator gen, World world, int cx, int cz) {
		TerrainGeneration tg = dim.getSettings(TerrainGeneration.class, false);
		if (tg == null || chunksGenerated) return;
		if (!logged) {
			Main.LOG.fatal("The chunk generator {} doesn't trigger {} when generating chunks!\nPlease report this issue to the mod author of the above mentionied ChunkGenerator.", gen.getClass().getName(), ReplaceBiomeBlocks.class.getName());
			Main.LOG.warn("Switching to inefficient terrain generation during chunk population.");
			logged = true;
		}
		fixInitialization(dim, world, gen);
		tg.generate(gen, PostGenChunkBuffer.wrap(world.getChunkFromChunkCoords(cx, cz)), cx, cz);
	}

	public void fixInitialization(IDimension dim, World world, IChunkGenerator gen) {
		if (!initialized) {
			Main.LOG.fatal("The chunk generator {} did not trigger {} during initialization!\nPlease report this issue to the mod author of the above mentionied ChunkGenerator.", gen.getClass().getName(), InitNoiseGensEvent.class.getName());
			Main.proxy.worldgenTerrain.init(new InitNoiseGensEvent<Context>(world, new Random(), new Context(null, null, null, null, null)));
			Main.LOG.warn("Could not provide native noise fields and RNG for dimension {}!\nTerrain features that depend on these won't work correctly.", dim);
			initialized = true;
		}
	}

}
