package cd4017be.dimstack.block;

import cd4017be.dimstack.PortalConfiguration;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.block.BaseBlock;
import cd4017be.lib.util.DimPos;
import cd4017be.lib.util.MovedBlock;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.isCreative() && player.isSneaking() && player.getHeldItem(hand).isEmpty()) {
			onBlockClicked(world, pos, player);
			return true;
		}
		if (state.getValue(solidOther1)) return false;
		if (!(world instanceof WorldServer)) return true;
		if (player instanceof EntityPlayerMP && !(player instanceof FakePlayer)) {
			ItemStack item = player.getHeldItem(hand);
			if (!item.isEmpty()) {
				DimPos posT = new DimPos(pos, world);
				DimPos posO = PortalConfiguration.getAdjacentPos(posT);
				if (posO == null) return false;
				syncStates(posO, posT);
				boolean ceil = pos.getY() < 128;
				posO = posO.add(0, (posT.getBlock().getValue(solidOther2) ? 1 : 2) * (ceil ? -1 : 1), 0);
				tryPlaceBlock(posO, player, item, hand, facing, hitX, hitY, hitZ);
				player.addExhaustion(4.0F);
				return true;
			}
		}
		return false;
	}

	/**
	 * Picked together from PlayerInteractionManager, with unwanted code removed.
	 */
	public void tryPlaceBlock(DimPos pos, EntityPlayer player, ItemStack stack, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Block block = pos.getBlock().getBlock();
		WorldServer world = pos.getWorldServer();
		if (!block.isReplaceable(world, pos)) return;
		
		net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
				.onRightClickBlock(player, hand, pos, facing, new Vec3d(hitX, hitY, hitZ));
		if (event.isCanceled()) return;
		
		if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
			Block block1 = ((ItemBlock)stack.getItem()).getBlock();
			if (block1 instanceof BlockCommandBlock || block1 instanceof BlockStructure)
				return;
		}
		
		if (event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
			return;
		
		if (player.isCreative()) {
			int j = stack.getMetadata();
			int i = stack.getCount();
			stack.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
			stack.setItemDamage(j);
			stack.setCount(i);
		} else {
			ItemStack copyBeforeUse = stack.copy();
			stack.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
			if (stack.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
		}
		int y = pos.getY();
		if (y <= 2) world.neighborChanged(pos.down(y), block, pos);
		else if (y >= 253) world.neighborChanged(pos.up(255-y), block, pos);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (world instanceof WorldServer && player instanceof EntityPlayerMP && !(player instanceof FakePlayer)) {
			if (!player.isCreative() && player.getFoodStats().getFoodLevel() <= 1) return;
			DimPos posT = new DimPos(pos, world);
			DimPos posO = PortalConfiguration.getAdjacentPos(posT);
			if (posO == null) return;
			syncStates(posO, posT);
			boolean ceil = pos.getY() < 128;
			IBlockState state = posT.getBlock();
			if (state.getValue(solidOther1))
				posO = posO.add(0, ceil ? -1 : 1, 0);
			else if (state.getValue(solidOther2))
				posO = posO.add(0, ceil ? -2 : 2, 0);
			else return;
			tryHarvestBlock(posO, (EntityPlayerMP) player, posT);
			player.addExhaustion(4.0F);
		}
	}

	/**
	 * Picked together from PlayerInteractionManager and some Forge methods, with unwanted code removed
	 * (such as sending data packets for wrong locations).
	 */
	private void tryHarvestBlock(DimPos pos, EntityPlayerMP player, DimPos orPos) {
		IBlockState state = pos.getBlock();
		WorldServer world = pos.getWorldServer();
		if (!player.isCreative() && state.getPlayerRelativeBlockHardness(player, world, pos) <= 0) return;

		// Logic from tryHarvestBlock for pre-canceling the event
		ItemStack stack = player.getHeldItemMainhand();
		Block block = state.getBlock();
		boolean preCancelEvent = player.isSpectator() || !player.isAllowEdit() && (stack.isEmpty() || !stack.canDestroy(block));

		// Post the block break event
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
		event.setCanceled(preCancelEvent);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return;

		TileEntity tileentity = pos.getTileEntity();

		if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !player.canUseCommandBlock()) return;
		if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, player)) return;

		//play sound where the player can hear it
		orPos.getWorld().playEvent(player, 2001, orPos, Block.getStateId(state));

		if (player.isCreative()) {
			if (!block.removedByPlayer(state, world, pos, player, false)) return;
			block.onBlockDestroyedByPlayer(world, pos, state);
		} else {
			ItemStack itemstack2 = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
			boolean flag = state.getBlock().canHarvestBlock(world, pos, player);
			if (!stack.isEmpty()) {
				stack.onBlockDestroyed(world, state, pos, player);
				if (stack.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack2, EnumHand.MAIN_HAND);
			}
			if (!block.removedByPlayer(state, world, pos, player, flag)) return;
			block.onBlockDestroyedByPlayer(world, pos, state);
			if (flag)
				block.harvestBlock(world, player, pos, state, tileentity, itemstack2);
			// Drop experience
			int exp = event.getExpToDrop();
			if (exp > 0)
				block.dropXpOnBlockBreak(world, pos, exp);
		}
		int y = pos.getY();
		if (y <= 2) world.neighborChanged(pos.down(y), block, pos);
		else if (y >= 253) world.neighborChanged(pos.up(255-y), block, pos);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (world instanceof WorldServer && !state.getValue(solidOther1) && !state.getValue(solidOther2)) {
			AxisAlignedBB box = entity.getEntityBoundingBox();
			boolean floor = pos.getY() < 128;
			double py = entity.motionY;
			if (floor) {
				if (box.maxY + py >= 1.0) return;
				py = entity.posY + 254.0;
				if (box.maxY > 1.0) py -= box.maxY - 1.0;
			} else {
				if (box.minY + py <= 255.0) return;
				py = entity.posY - 254.0;
				if (box.minY < 255.0) py -= box.minY - 255.0;
			}
			DimPos posT = new DimPos(pos, world);
			DimPos posO = PortalConfiguration.getAdjacentPos(posT);
			if (posO == null) return;
			syncStates(posT, posO);
			int dim = posO.dimId;
			double x = entity.posX, y = py, z = entity.posZ;
			TickRegistry.instance.updates.add(()-> {
				//only teleport if not already at destination (to avoid duplicate events)
				if (entity.dimension != dim || Math.abs(entity.posY - y) > entity.height + 4.0) {
					MovedBlock.moveEntity(entity, dim, x, y, z);
				}
			});
		}
	}

	private void syncStates(DimPos oPos, DimPos tPos) {
		IBlockState oState = oPos.getBlock();
		IBlockState tState = tPos.getBlock();
		if (oState.getBlock() != this) return;
		int ofs = tPos.getY() < 128 ? 2 : -2;
		boolean sT = isSolid(tPos.getWorld(), tPos.up(ofs)),
				sO = isSolid(oPos.getWorld(), oPos.down(ofs));
		IBlockState ntState = tState.withProperty(solidThis2, sT);
		IBlockState noState = oState.withProperty(solidThis2, sO);
		ntState = tState.withProperty(solidOther2, sO)
				.withProperty(solidOther1, oState.getValue(solidThis1));
		noState = oState.withProperty(solidOther2, sT)
				.withProperty(solidOther1, tState.getValue(solidThis1));
		if (ntState != tState)
			tPos.setBlock(ntState);
		if (noState != oState)
			oPos.setBlock(noState);
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
			if ((state.getValue(solidThis1) ^ this1) || (state.getValue(solidThis2) ^ this2) && Utils.neighboursLoaded(world, pos))
				syncStates(new DimPos(pos, world), state, this1, this2);
		} else if (TickRegistry.instance.updates.size() < 100) {
			TickRegistry.instance.updates.add(()-> neighborChanged(state, world, pos, this, pos));
		}
	}

	public void syncStates(DimPos posT, IBlockState stateT, boolean this1, boolean this2) {
		DimPos posO = PortalConfiguration.getAdjacentPos(posT);
		if (posO == null) return;
		IBlockState stateO = posO.getBlock();
		if (stateO.getBlock() != this) stateO = getDefaultState().withProperty(onCeiling, posO.getY() >= 128);
		int ceil = posO.getY() >= 128 ? -1 : 1;
		boolean other1 = isSolid(posO.getWorld(), posO.up(ceil)), other2 = isSolid(posO.getWorld(), posO.up(ceil<<1));
		posT.setBlock(stateT.withProperty(solidThis1, this1).withProperty(solidThis2, this2).withProperty(solidOther1, other1).withProperty(solidOther2, other2));
		posO.setBlock(stateO.withProperty(solidThis1, other1).withProperty(solidThis2, other2).withProperty(solidOther1, this1).withProperty(solidOther2, this2));
	}

	public static boolean isSolid(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos).getMaterial().blocksMovement();
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
		return state.getValue(solidOther1) ? state.getValue(solidOther2) ? 255 : 3 : state.getValue(solidOther2) ? 1 : 0;
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
		case UP: return ceil || !(state.getValue(solidOther1) && world.getBlockState(pos1).doesSideBlockRendering(world, pos1, EnumFacing.DOWN));
		case DOWN: return !ceil || !(state.getValue(solidOther1) && world.getBlockState(pos1).doesSideBlockRendering(world, pos1, EnumFacing.UP));
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

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

}
