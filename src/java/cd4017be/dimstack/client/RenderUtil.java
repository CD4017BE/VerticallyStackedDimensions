package cd4017be.dimstack.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author CD4017BE
 *
 */
@SideOnly(Side.CLIENT)
public class RenderUtil {

	public static void renderBlock(IBlockAccess world, BlockPos pos, BufferBuilder bb) {
		BlockRendererDispatcher render = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = world.getBlockState(pos);
		render.renderBlock(state, pos, world, bb);
	}

	public static class BlockWrapper implements IBlockAccess {
		private static final IBlockState BG_STATE = Blocks.AIR.getDefaultState();

		public IBlockState state;

		public BlockWrapper(IBlockState state) {
			this.state = state;
		}

		@Override
		public TileEntity getTileEntity(BlockPos pos) {
			return null;
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			return 0xf00000 | lightValue << 4;
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			return pos.equals(BlockPos.ORIGIN) ? state : BG_STATE;
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			return !pos.equals(BlockPos.ORIGIN) || state.getBlock().isAir(state, this, pos);
		}

		@Override
		public Biome getBiome(BlockPos pos) {
			return Biomes.DEFAULT;
		}

		@Override
		public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return 0;
		}

		@Override
		public WorldType getWorldType() {
			return WorldType.DEBUG_ALL_BLOCK_STATES;
		}

		@Override
		public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			return pos.equals(BlockPos.ORIGIN) && state.isSideSolid(this, pos, side);
		}

	}

}
