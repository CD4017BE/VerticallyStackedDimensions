package cd4017be.dimstack.asm;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;


/**
 * @author CD4017BE
 *
 */
@IFMLLoadingPlugin.TransformerExclusions(value = {"cd4017be.dimstack.asm"})
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
@IFMLLoadingPlugin.Name(value = "Vertically Stacked Dimensions ASM")
@IFMLLoadingPlugin.SortingIndex(value = 2000)
public class CorePlugin implements IFMLLoadingPlugin {

	public static final Logger LOG = LogManager.getLogger("VSD ASM");

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{
				"cd4017be.dimstack.asm.ChunkPrimerTransformer",
				"cd4017be.dimstack.asm.BlockPortalTransformer"
			};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
