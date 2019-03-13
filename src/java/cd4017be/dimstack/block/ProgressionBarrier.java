package cd4017be.dimstack.block;

import java.util.function.Predicate;

import cd4017be.lib.block.BaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;


/**
 * @author CD4017BE
 *
 */
public class ProgressionBarrier extends BaseBlock {

	public static final PropertyInteger variant = PropertyInteger.create("var", 0, 15);
	public Predicate<ItemStack>[] requiredTools;
	public float[] hardness, resistance;
	public short mobBreakable;

	/**
	 * @param id
	 * @param m
	 */
	public ProgressionBarrier(String id, Material m) {
		super(id, m);
	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		Predicate<ItemStack> p = requiredTools[state.getValue(variant)];
		if (!p.test(player.getHeldItemMainhand()) && !p.test(player.getHeldItemOffhand())) return 0;
		return ForgeHooks.blockStrength(state, player, world, pos);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(variant, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(variant);
	}

	@Override
	public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
		return hardness[state.getValue(variant)];
	}

	@Override
	public BlockStateContainer getBlockState() {
		return new BlockStateContainer(this, variant);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return resistance[world.getBlockState(pos).getValue(variant)] / 5.0F;
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return (mobBreakable >> state.getValue(variant) & 1) != 0;
	}

}
