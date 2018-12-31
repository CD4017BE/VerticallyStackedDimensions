package cd4017be.dimstack.block;

import cd4017be.dimstack.PortalConfiguration;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.block.BaseBlock;
import cd4017be.lib.util.MovedBlock;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author cd4017be
 */
public class Portal extends BaseBlock {

	public static final PropertyBool
		solidThis1 = PropertyBool.create("this1"),
		solidThis2 = PropertyBool.create("this2"),
		solidOther1 = PropertyBool.create("other1"),
		solidOther2 = PropertyBool.create("other2"),
		onCeiling = PropertyBool.create("ceil");

	private static final AxisAlignedBB
		fullCeil = new AxisAlignedBB(0, 0, 0, 1, 2, 1),
		halfCeil = new AxisAlignedBB(0, 1, 0, 1, 2, 1),
		emptyCeil = new AxisAlignedBB(0, 1.9375, 0, 1, 2, 1),
		fullFloor = new AxisAlignedBB(0, -1, 0, 1, 1, 1),
		halfFloor = new AxisAlignedBB(0, -1, 0, 1, 0, 1),
		emptyFloor = new AxisAlignedBB(0, -1, 0, 1, -0.9375, 1);

	public Portal(String id) {
		super(id, Material.ROCK);
		this.setBlockUnbreakable();
		this.setResistance(Float.POSITIVE_INFINITY);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(solidOther1, (meta&1) != 0)
				.withProperty(solidOther2, (meta&2) != 0)
				.withProperty(solidThis1, (meta&4) != 0)
				.withProperty(solidThis2, (meta&8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(solidOther1) ? 1:0)
				| (state.getValue(solidOther2) ? 2:0)
				| (state.getValue(solidThis1) ? 4:0)
				| (state.getValue(solidThis2) ? 8:0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {solidThis1, solidThis2, solidOther1, solidOther2, onCeiling});
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(onCeiling, pos.getY() >= 128);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (world instanceof WorldServer && !state.getValue(solidOther1) && !state.getValue(solidOther2)) {
			AxisAlignedBB box = entity.getEntityBoundingBox();
			PortalConfiguration pc = PortalConfiguration.get((WorldServer) world);
			boolean floor = pos.getY() < 128;
			double py = entity.motionY;
			if (floor) {
				if (box.maxY + py >= 1.0) return;
				pc = pc.neighbourDown;
				py = entity.posY + 254.0;
				if (box.maxY > 1.0) py -= box.maxY - 1.0;
			} else {
				if (box.minY + py <= 255.0) return;
				pc = pc.neighbourUp;
				py = entity.posY - 254.0;
				if (box.minY < 255.0) py -= box.minY - 255.0;
			}
			if (pc == null) return;
			MovedBlock.moveEntity(entity, pc.dimId, entity.posX, py, entity.posZ);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (pos.getY() > 0 && pos.getY() < 255 || !(placer instanceof EntityPlayer && ((EntityPlayer)placer).isCreative())) {
			placer.sendMessage(new TextComponentString("This block is meant to be auto generated at the bottom (y=0) and/or top (y=255) most layer of the world!"));
			world.setBlockToAir(pos);
			return;
		}
		this.neighborChanged(state, world, pos, this, pos);//Initialize state
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!(world instanceof WorldServer)) return;
		if (fromPos.getX() == pos.getX() && fromPos.getZ() == pos.getZ()) {
			int ceil = pos.getY() >= 128 ? -1 : 1;
			boolean this1 = isSolid(world, pos.up(ceil)), this2 = isSolid(world, pos.up(ceil<<1));
			if ((state.getValue(solidThis1) ^ this1) || (state.getValue(solidThis2) ^ this2))
				syncStates((WorldServer) world, pos, state, this1, this2);
		} else if (TickRegistry.instance.updates.size() < 100) {
			TickRegistry.instance.updates.add(()-> neighborChanged(state, world, pos, this, pos));
		}
	}

	private void syncStates(WorldServer world, BlockPos pos, IBlockState state, boolean this1, boolean this2) {
		if (!Utils.neighboursLoaded(world, pos)) return;
		WorldServer otherW = PortalConfiguration.getAdjacentWorld(world, pos);
		if (otherW == null) return;
		BlockPos otherP = PortalConfiguration.getAdjacentPos(pos);
		IBlockState otherS = otherW.getBlockState(otherP);
		if (otherS.getBlock() != this) otherS = getDefaultState().withProperty(onCeiling, otherP.getY() >= 128);
		int ceil = otherP.getY() >= 128 ? -1 : 1;
		boolean other1 = isSolid(otherW, otherP.up(ceil)), other2 = isSolid(otherW, otherP.up(ceil<<1));
		world.setBlockState(pos, state.withProperty(solidThis1, this1).withProperty(solidThis2, this2).withProperty(solidOther1, other1).withProperty(solidOther2, other2));
		otherW.setBlockState(otherP, otherS.withProperty(solidThis1, other1).withProperty(solidThis2, other2).withProperty(solidOther1, this1).withProperty(solidOther2, this2));
	}

	public static boolean isSolid(IBlockAccess world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getMaterial().blocksMovement();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		boolean ceil = pos.getY() >= 128;
		return state.getValue(solidOther1) ? (ceil ? fullCeil : fullFloor)
			: state.getValue(solidOther2) ? (ceil? halfCeil : halfFloor)
										: (ceil ? emptyCeil : emptyFloor);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		boolean ceil = pos.getY() >= 128;
		return state.getValue(solidOther1) ? (ceil ? fullCeil : fullFloor)
			: state.getValue(solidOther2) ? (ceil? halfCeil : halfFloor)
			: NULL_AABB;
	}

	@Override
	public int getLightOpacity(IBlockState state) {
		return state.getValue(solidOther1) ? 255 : state.getValue(solidOther2) ? 1 : 0;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(solidOther1) || state.getValue(solidOther2) ? 0 : 8;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		BlockPos pos1 = pos.offset(side);
		boolean ceil = pos.getY() >= 128;
		switch(side) {
		case UP: return ceil || !world.getBlockState(pos1).doesSideBlockRendering(world, pos1, EnumFacing.DOWN);
		case DOWN: return !ceil || !world.getBlockState(pos1).doesSideBlockRendering(world, pos1, EnumFacing.UP);
		default:
			IBlockState other = world.getBlockState(pos1);
			if (other.getBlock() != this) return !other.doesSideBlockRendering(world, pos1, side.getOpposite());
			return state.getValue(solidOther1) && !other.getValue(solidOther1)
				|| state.getValue(solidOther2) && !other.getValue(solidOther2);
		}
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return state.getValue(solidOther1);
	}

	public boolean isFullyOpaque(IBlockState state) {
		return state.getValue(solidOther1);
	}

	public boolean isOpaqueCube(IBlockState state) {
		return state.getValue(solidOther1);
	}

	public boolean isFullCube(IBlockState state) {
		return state.getValue(solidOther1);
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return state.getValue(solidOther1);
	}

	@Override
	public boolean causesSuffocation(IBlockState state) {
		return state.getValue(solidOther1);
	}

}
