package cd4017be.dimstack.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

	public static void drawPortrait(IBlockState state, int x, int y, float z, int h) {
		GlStateManager.pushMatrix();
		float w = (float)h / 2F, s = (float)h * 0.71F;
		GlStateManager.translate(x + w, y + w, z);
		GlStateManager.scale(s, -s, s);
		GlStateManager.rotate(15, 1, 1, 0);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		BlockWrapper world = new BlockWrapper(state);
		BufferBuilder bb = Tessellator.getInstance().getBuffer();
		bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		RenderUtil.renderBlock(world, BlockPos.ORIGIN, bb);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
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
