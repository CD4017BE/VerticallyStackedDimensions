package cd4017be.dimstack.api;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;


/**
 * Holds a list of custom noise generators and a random generator that are shared across all dimensions.
 * @author CD4017BE
 */
public class SharedNoiseFields implements IDimensionSettings {
	private static final long MAGIC = 0xCD4017BE;

	public byte[] octaves;
	public NoiseGenerator[] noiseFields;
	public Random rand;

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByteArray("octaves", octaves);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.octaves = nbt.getByteArray("octaves");
		this.noiseFields = new NoiseGenerator[octaves.length];
	}

	public void init(long seed) {
		if (rand != null) return;
		this.rand = new Random(seed + MAGIC);
		for (int i = 0, n = octaves.length; i < n; i++) {
			int o = octaves[i];
			if (o > 0) noiseFields[i] = new NoiseGeneratorOctaves(rand, o);
			else noiseFields[i] = new NoiseGeneratorPerlin(rand, -o);
		}
	}

}
