package cd4017be.dimstack.core;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.Objects;
import cd4017be.dimstack.api.CustomWorldProps;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;

/** @author CD4017BE */
public class WorldProviderCustom extends WorldProvider {

	PortalConfiguration stack;

	@Override
	public void setDimension(int dim) {
		super.setDimension(dim);
		this.stack = PortalConfiguration.get(dim);
	}

	@Override
	public DimensionType getDimensionType() {
		return Objects.CUSTOM_DIM_TYPE;
	}

	@Override
	protected void init() {
		CustomWorldProps props = stack.getSettings(CustomWorldProps.class, true);
		this.hasSkyLight = props.skylight;
		this.doesWaterVaporize = props.noWater;
		this.nether = props.netherlike;
		if (props.biomeGen.isEmpty())
			this.biomeProvider = world.getWorldType().getBiomeProvider(world);
		else {
			Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(props.biomeGen));
			if (biome == null) {
				Main.LOG.error("Invalid biome id {}, falling back to single biome plains instead!", props.biomeGen);
				biome = Biomes.PLAINS;
			}
			this.biomeProvider = new BiomeProviderSingle(biome);
		}
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		CustomWorldProps props = stack.getSettings(CustomWorldProps.class, true);
		WorldInfo info = world.getWorldInfo();
		switch(props.chunkGen) {
		default:
			return new ChunkGeneratorVoid(world, world.getSeed());
		case 1:
			return super.createChunkGenerator();
		case 2:
			return new ChunkGeneratorOverworld(world, world.getSeed(), info.isMapFeaturesEnabled(), info.getGeneratorOptions());
		case 3:
			return new ChunkGeneratorHell(world, info.isMapFeaturesEnabled(), world.getSeed());
		}
		
	}

	@Override
	public int getActualHeight() {
		return stack.ceilY;
	}

	@Override
	public int getHeight() {
		return stack.ceilY + 1;
	}

	@Override
	public boolean isSurfaceWorld() {
		return stack.getSettings(CustomWorldProps.class, true).visibleSky;
	}

	@Override
	public boolean shouldClientCheckLighting() {
		return !hasSkyLight;
	}

}
