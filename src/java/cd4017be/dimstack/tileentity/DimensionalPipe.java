package cd4017be.dimstack.tileentity;

import cd4017be.dimstack.Objects;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.DimPos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

/**
 * 
 * @author cd4017be
 */
public class DimensionalPipe extends BaseTileEntity implements INeighborAwareTile, IUpdatable, IInteractiveTile {

	private static final byte ITEM = 1, FLUID = 2;
	private static final Object[] NULL_CAPS = {null, EmptyHandler.INSTANCE, EmptyFluidHandler.INSTANCE};

	protected EnumFacing side;
	protected DimensionalPipe linkTile;
	protected IBlockState conBlock = Blocks.AIR.getDefaultState();
	private byte hasCap;
	private boolean updateLink, updateCon;

	protected void link(DimensionalPipe tile) {
		if (tile != linkTile) {
			linkTile = tile;
			if (!unloaded && tile != null)
				onConTileChange(tile.world.getBlockState(tile.pos.offset(tile.side)));
		}
		updateLink = false;
	}

	@Override
	public void process() {
		if (unloaded) return;
		if (updateLink) {
			updateLink = false;
			DimPos linkPos = PortalConfiguration.getAdjacentPos(new DimPos(this), false);
			if (linkPos == null) return;
			TileEntity te = linkPos.getTileEntity();
			if (te instanceof DimensionalPipe) {
				link((DimensionalPipe)te);
				linkTile.link(this);
			} else {
				link(null);
			}
		}
		if (updateCon) {
			if (linkTile != null && !linkTile.unloaded)
				linkTile.onConTileChange(world.getBlockState(pos.offset(side)));
			updateCon = false;
		}
	}

	private void onConTileChange(IBlockState state) {
		if (conBlock != state) {
			conBlock = state;
			world.neighborChanged(pos.offset(side), blockType, pos);
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		updateLink = updateCon = true;
		if (!world.isRemote) TickRegistry.instance.updates.add(this);
		side = pos.getY() == 0 ? EnumFacing.UP : EnumFacing.DOWN;
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if (linkTile != null && linkTile.linkTile == this) linkTile.link(null);
		linkTile = null;
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		if (!updateCon && pos.offset(side).equals(src)) {
			updateCon = true;
			TickRegistry.instance.updates.add(this);
		}
	}

	public void neighborTileChange(TileEntity te, EnumFacing side) {
	}

	private TileEntity getCon() {
		return world.getTileEntity(pos.offset(side));
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
		if (facing != side) return false;
		byte type = cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? ITEM :
			cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? FLUID : 0;
		if (linkTile != null && !linkTile.unloaded) {
			TileEntity te = linkTile.getCon();
			if (te != null && te.hasCapability(cap, facing)) {
				hasCap |= type;
				return true;
			} else {
				hasCap &= ~type;
				return false;
			}
		} else return (hasCap & type) != 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
		if (facing != side) return null;
		byte type = cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? ITEM :
			cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? FLUID : 0;
		if (linkTile != null && !linkTile.unloaded) {
			TileEntity te = linkTile.getCon();
			T c = te != null ? te.getCapability(cap, facing) : null;
			if (c != null) hasCap |= type;
			else hasCap &= ~type;
			return c;
		}
		if ((hasCap & type) != 0)
			return (T) NULL_CAPS[type];
		return null;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) {
		if (!player.isSneaking() || !item.isEmpty()) return false;
		DimPos posT = new DimPos(this), posO = PortalConfiguration.getAdjacentPos(posT);
		if (posO != null) {
			posO.setBlock(Objects.PORTAL.getDefaultState());
			Objects.PORTAL.syncStates(posT, Objects.PORTAL.getDefaultState());
			player.addItemStackToInventory(new ItemStack(Objects.dim_pipe));
		}
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		nbt.setShort("con", (short) Block.getStateId(conBlock));
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		conBlock = Block.getStateById(nbt.getShort("con"));
	}

}
