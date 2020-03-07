package cd4017be.dimstack.core;

import cd4017be.dimstack.Main;
import cd4017be.dimstack.Objects;
import cd4017be.dimstack.api.CustomWorldProps;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import static cd4017be.dimstack.api.CustomWorldProps.*;

/** @author CD4017BE */
public class WorldProviderCustom extends WorldProvider {

	PortalConfiguration stack;
	CustomWorldProps cfg;

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
		this.cfg = stack.getSettings(CustomWorldProps.class, true);
		this.hasSkyLight = (cfg.flags & F_SKYLIGHT) != 0;
		this.doesWaterVaporize = (cfg.flags & F_WATEREVAP) != 0;
		this.nether = (cfg.flags & F_NETHER) != 0;
		if (cfg.biomeGen.isEmpty())
			this.biomeProvider = world.getWorldType().getBiomeProvider(world);
		else {
			Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(cfg.biomeGen));
			if (biome == null) {
				Main.LOG.error("Invalid biome id {}, falling back to single biome plains instead!", cfg.biomeGen);
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
		return (cfg.flags & F_SKYBOX) != 0;
	}

	@Override
	public boolean shouldClientCheckLighting() {
		return !hasSkyLight;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesXZShowFog(int x, int z) {
		return (cfg.flags & F_FOG) != 0;
	}

	@Override
	public double getVoidFogYFactor() {
		return (double)(cfg.fogColor >>> 24) / 256D;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(float celAng, float t) {
        float f1 = (cfg.fogColor >> 16 & 0xff) / 255F;
        float f2 = (cfg.fogColor >> 8 & 0xff) / 255F;
        float f3 = (cfg.fogColor & 0xff) / 255F;
        if ((cfg.flags & F_SKYBOX) != 0) {
        	float f = MathHelper.cos(celAng * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        	f = MathHelper.clamp(f, 0.0F, 1.0F);
        	f1 = f1 * (f * 0.94F + 0.06F);
        	f2 = f2 * (f * 0.94F + 0.06F);
        	f3 = f3 * (f * 0.91F + 0.09F);
        }
        return new Vec3d((double)f1, (double)f2, (double)f3);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		return cfg.cloudHeight;
	}

	@Override
	public double getHorizon() {
		return cfg.horizonHeight;
	}

}
