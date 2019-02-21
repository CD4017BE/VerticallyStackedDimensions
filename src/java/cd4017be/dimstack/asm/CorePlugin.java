package cd4017be.dimstack.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;


/**
 * @author CD4017BE
 *
 */
@IFMLLoadingPlugin.TransformerExclusions(value = {"cd4017be.dimstack.asm"})
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
@IFMLLoadingPlugin.Name(value = "Vertically Stacked Dimensions ASM")
public class CorePlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"cd4017be.dimstack.asm.ChunkPrimerTransformer"};
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
