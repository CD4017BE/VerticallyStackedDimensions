package cd4017be.dimstack.client.gui;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

/** @author CD4017BE */
public class BlockStateCompletion extends AutoCompletion<Block> {

	public BlockStateCompletion(int n) {
		super(n, Block.REGISTRY);
	}

	@Override
	protected String addExtension(ResourceLocation id) {
		return id.toString() + "@";
	}

	@Override
	protected boolean searchExtensions(String s) {
		int p1 = s.indexOf('@');
		if(p1 < 0) return false;
		int n = 0;
		String meta = s.substring(p1 + 1);
		s = s.substring(0, p1);
		Block block = Block.REGISTRY.getObject(new ResourceLocation(s));
		int states = 0;
		for(IBlockState state : block.getBlockState().getValidStates())
			states |= 1 << block.getMetaFromState(state);
		for(int j = 0; states != 0; j++, states >>>= 1) {
			if((states & 1) == 0) continue;
			String m = Integer.toString(j);
			if(m.startsWith(meta)) {
				results[n++] = s + "@" + m;
				if(n == results.length) break;
			}
		}
		lastFilled = n;
		return true;
	}

}
