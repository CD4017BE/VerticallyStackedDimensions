package cd4017be.dimstack.api;

import java.util.Random;

import cd4017be.dimstack.api.util.NoiseField;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * Holds a list of custom noise generators and a random generator that are shared across all dimensions.
 * @author CD4017BE
 */
public class SharedNoiseFields implements IDimensionSettings {
	private static final long MAGIC = 0xCD4017BE;

	public byte[] octaves, source;
	public NoiseGenerator[] noiseGens;
	public Random rand;
	public NoiseField[] noiseFields;

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByteArray("octaves", octaves);
		NBTTagList list = new NBTTagList();
		int n = 0;
		for (NoiseField nf : noiseFields) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setByte("hg", (byte)nf.hGrid);
			tag.setByte("vg", (byte)nf.vGrid);
			tag.setDouble("hs", nf.hScale);
			tag.setDouble("vs", nf.vScale);
			tag.setByte("src", source[n++]);
			list.appendTag(tag);
		}
		nbt.setTag("fields", list);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTBase tag) {
		NBTTagCompound nbt = (NBTTagCompound)tag;
		this.octaves = nbt.getByteArray("octaves");
		this.noiseGens = new NoiseGenerator[octaves.length];
		NBTTagList list = nbt.getTagList("fields", NBT.TAG_COMPOUND);
		this.source = new byte[list.tagCount()];
		this.noiseFields = new NoiseField[source.length];
		for (int i = 0; i < source.length; i++) {
			NBTTagCompound ctag = list.getCompoundTagAt(i);
			source[i] = ctag.getByte("src");
			noiseFields[i] = new NoiseField(ctag.getByte("hg"), ctag.getByte("vg") & 0xff, ctag.getDouble("hs"), ctag.getDouble("vs"));
		}
	}

	public void init(long seed) {
		if (rand != null) return;
		this.rand = new Random(seed + MAGIC);
		if (noiseGens == null) noiseGens = new NoiseGenerator[octaves.length];
		for (int i = 0, n = octaves.length; i < n; i++) {
			int o = octaves[i];
			if (o > 0) noiseGens[i] = new NoiseGeneratorOctaves(rand, o);
			else noiseGens[i] = new NoiseGeneratorPerlin(rand, -o);
		}
		for (int i = 0, n = source.length; i < n; i++) {
			byte s = source[i];
			if (s >= 0 && s < noiseGens.length)
				noiseFields[i].setGenerator(noiseGens[i]);
		}
	}

}
