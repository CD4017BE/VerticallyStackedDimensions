package cd4017be.dimstack;

import java.io.File;
import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.dimstack.api.DisabledPortals;
import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.block.ProgressionBarrier;
import cd4017be.dimstack.core.ChunkLoader;
import cd4017be.dimstack.core.Dimensionstack;
import cd4017be.dimstack.worldgen.BlockReplacer;
import cd4017be.dimstack.worldgen.OreGenHandler;
import cd4017be.dimstack.worldgen.PortalGen;
import cd4017be.dimstack.worldgen.TerrainGenHandler;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.util.FileUtil;

/**
 * 
 * @author cd4017be
 */
public class CommonProxy {

	public PortalGen worldgenPortal;
	public BlockReplacer worldgenBedrock;
	public TerrainGenHandler worldgenTerrain;
	public OreGenHandler worldgenOres;

	public void init() {
		TickRegistry.register();
		
		worldgenTerrain = new TerrainGenHandler();
		worldgenPortal = new PortalGen();
		worldgenBedrock = new BlockReplacer();
		worldgenOres = new OreGenHandler();
		setConfig(new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName)));
	}

	protected void setConfig(ConfigConstants cfg) {
		String override = cfg.get("override_default", String.class, null);
		if (override != null && Main.dimstack.loadPreset(new File(FileUtil.configDir, override)))
			Main.LOG.info("Dimension stack default configuration initialized from preset file. Config settings will be ignored!");
		else {
			Dimensionstack.initConfig(cfg);
			worldgenTerrain.initConfig(cfg);
			worldgenOres.initConfig(cfg);
		}
		ChunkLoader.initConfig(cfg);
		worldgenPortal.initConfig(cfg);
		cfg.get("barrier_block", ProgressionBarrier.class, Objects.BEDROCK);
		if (cfg.getNumber("disable_nether_portal", 0) != 0)
			Main.dimstack.getSettings(DisabledPortals.class, true).add("nether");
		Portal.CREATE_PLATTFORM = (int)cfg.getNumber("create_plattform", Portal.CREATE_PLATTFORM);
	}

	public void registerRenderers() {
	}

}
