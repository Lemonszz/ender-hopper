package party.lemons.enderhopper;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.*;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TileEntityEnderHopper extends TileEntityHopper
{
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	private int transferCooldown = -1;
	private long tickedGameTime;
	private UUID owner = null;

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

		if(compound.hasKey("owner"))
			this.owner = NBTUtil.getUUIDFromTag(compound.getCompoundTag("owner"));

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound = super.writeToNBT(compound);

		if(owner != null)
			compound.setTag("owner", NBTUtil.createUUIDTag(owner));

		return compound;
	}

	@Override
	protected boolean updateHopper()
	{
		if (this.world != null && !this.world.isRemote)
		{
			if (!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata()))
			{
				boolean flag = false;

				if (!this.isInventoryEmpty())
				{
					flag = this.transferItemsOut();
				}

				if (!this.isFull())
				{
					flag = pullItems(this, owner) || flag;
				}

				if (flag)
				{
					this.setTransferCooldown(8);
					this.markDirty();
					return true;
				}
			}

			return false;
		}
		else
		{
			return false;
		}
	}

	private boolean isInventoryEmpty()
	{
		for (ItemStack itemstack : this.inventory)
		{
			if (!itemstack.isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	private boolean isFull()
	{
		for (ItemStack itemstack : this.inventory)
		{
			if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize())
			{
				return false;
			}
		}

		return true;
	}

	private boolean transferItemsOut()
	{
		IInventory iinventory = this.getInventoryForHopperTransfer();

		if (iinventory == null)
		{
			return false;
		}
		else
		{
			EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();

			if (this.isInventoryFull(iinventory, enumfacing))
			{
				return false;
			}
			else
			{
				for (int i = 0; i < this.getSizeInventory(); ++i)
				{
					if (!this.getStackInSlot(i).isEmpty())
					{
						ItemStack itemstack = this.getStackInSlot(i).copy();
						ItemStack itemstack1 = putStackInInventoryAllSlots(this, iinventory, this.decrStackSize(i, 1), enumfacing);

						if (itemstack1.isEmpty())
						{
							iinventory.markDirty();
							return true;
						}

						this.setInventorySlotContents(i, itemstack);
					}
				}

				return false;
			}
		}
	}

	/**
	 * Returns false if the inventory has any room to place items in
	 */
	private boolean isInventoryFull(IInventory inventoryIn, EnumFacing side)
	{
		if (inventoryIn instanceof ISidedInventory)
		{
			ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
			int[] aint = isidedinventory.getSlotsForFace(side);

			for (int k : aint)
			{
				ItemStack itemstack1 = isidedinventory.getStackInSlot(k);

				if (itemstack1.isEmpty() || itemstack1.getCount() != itemstack1.getMaxStackSize())
				{
					return false;
				}
			}
		}
		else
		{
			int i = inventoryIn.getSizeInventory();

			for (int j = 0; j < i; ++j)
			{
				ItemStack itemstack = inventoryIn.getStackInSlot(j);

				if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize())
				{
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns false if the specified IInventory contains any items
	 */
	private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side)
	{
		if (inventoryIn instanceof ISidedInventory)
		{
			ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
			int[] aint = isidedinventory.getSlotsForFace(side);

			for (int i : aint)
			{
				if (!isidedinventory.getStackInSlot(i).isEmpty())
				{
					return false;
				}
			}
		}
		else
		{
			int j = inventoryIn.getSizeInventory();

			for (int k = 0; k < j; ++k)
			{
				if (!inventoryIn.getStackInSlot(k).isEmpty())
				{
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Returns the IInventory that this hopper is pointing into
	 */
	private IInventory getInventoryForHopperTransfer()
	{
		EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata());
		return getInventoryAtPosition(this.getWorld(), this.getXPos() + (double)enumfacing.getFrontOffsetX(), this.getYPos() + (double)enumfacing.getFrontOffsetY(), this.getZPos() + (double)enumfacing.getFrontOffsetZ(), owner);
	}

	public static boolean pullItems(IHopper hopper, UUID owner)
	{
		Boolean ret = net.minecraftforge.items.VanillaInventoryCodeHooks.extractHook(hopper);
		if (ret != null) return ret;
		IInventory iinventory = getSourceInventory(hopper, owner);

		if (iinventory != null)
		{
			EnumFacing enumfacing = EnumFacing.DOWN;

			if (isInventoryEmpty(iinventory, enumfacing))
			{
				return false;
			}

			if (iinventory instanceof ISidedInventory)
			{
				ISidedInventory isidedinventory = (ISidedInventory)iinventory;
				int[] aint = isidedinventory.getSlotsForFace(enumfacing);

				for (int i : aint)
				{
					if (pullItemFromSlot(hopper, iinventory, i, enumfacing))
					{
						return true;
					}
				}
			}
			else
			{
				int j = iinventory.getSizeInventory();

				for (int k = 0; k < j; ++k)
				{
					if (pullItemFromSlot(hopper, iinventory, k, enumfacing))
					{
						return true;
					}
				}
			}
		}
		else
		{
			for (EntityItem entityitem : getCaptureItems(hopper.getWorld(), hopper.getXPos(), hopper.getYPos(), hopper.getZPos()))
			{
				if (putDropInInventoryAllSlots((IInventory)null, hopper, entityitem))
				{
					return true;
				}
			}
		}

		return false;
	}
	private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side)
	{
		return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
	}

	private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction)
	{
		ItemStack itemstack = inventoryIn.getStackInSlot(index);

		if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction))
		{
			ItemStack itemstack1 = itemstack.copy();
			ItemStack itemstack2 = putStackInInventoryAllSlots(inventoryIn, hopper, inventoryIn.decrStackSize(index, 1), (EnumFacing)null);

			if (itemstack2.isEmpty())
			{
				inventoryIn.markDirty();
				return true;
			}

			inventoryIn.setInventorySlotContents(index, itemstack1);
		}

		return false;
	}

	/**
	 * Gets the inventory that the provided hopper will transfer items from.
	 */
	public static IInventory getSourceInventory(IHopper hopper, UUID owner)
	{
		return getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0D, hopper.getZPos(), owner);
	}

	public static List<EntityItem> getCaptureItems(World worldIn, double p_184292_1_, double p_184292_3_, double p_184292_5_)
	{
		return worldIn.<EntityItem>getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(p_184292_1_ - 0.5D, p_184292_3_, p_184292_5_ - 0.5D, p_184292_1_ + 0.5D, p_184292_3_ + 1.5D, p_184292_5_ + 0.5D), EntitySelectors.IS_ALIVE);
	}

	/**
	 * Returns the IInventory (if applicable) of the TileEntity at the specified position
	 */
	public static IInventory getInventoryAtPosition(World worldIn, double x, double y, double z, UUID owner)
	{
		IInventory iinventory = null;
		int i = MathHelper.floor(x);
		int j = MathHelper.floor(y);
		int k = MathHelper.floor(z);
		BlockPos blockpos = new BlockPos(i, j, k);
		net.minecraft.block.state.IBlockState state = worldIn.getBlockState(blockpos);
		Block block = state.getBlock();

		if(block instanceof BlockEnderChest)
		{
			EntityPlayerMP playerMP = worldIn.getMinecraftServer().getPlayerList().getPlayerByUUID(owner);
			if(playerMP != null)
			{
				iinventory = playerMP.getInventoryEnderChest();
			}
		}
		else if (block.hasTileEntity(state))
		{
			TileEntity tileentity = worldIn.getTileEntity(blockpos);

			if (tileentity instanceof IInventory)
			{
				iinventory = (IInventory)tileentity;

				if (iinventory instanceof TileEntityChest && block instanceof BlockChest)
				{
					iinventory = ((BlockChest)block).getContainer(worldIn, blockpos, true);
				}
			}
		}

		if (iinventory == null)
		{
			List<Entity> list = worldIn.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);

			if (!list.isEmpty())
			{
				iinventory = (IInventory)list.get(worldIn.rand.nextInt(list.size()));
			}
		}

		return iinventory;
	}


	public void setTransferCooldown(int ticks)
	{
		this.transferCooldown = ticks;
	}

	private boolean isOnTransferCooldown()
	{
		return this.transferCooldown > 0;
	}

	public boolean mayTransfer()
	{
		return this.transferCooldown > 8;
	}

	public String getGuiID()
	{
		return "enderhopper:enderhopper";
	}

	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
	{
		this.fillWithLoot(playerIn);
		return new ContainerHopper(playerInventory, this, playerIn);
	}

	protected NonNullList<ItemStack> getItems()
	{
		return this.inventory;
	}

	public long getLastUpdateTime() { return tickedGameTime; } // Forge

	public void setOwner(UUID owner)
	{
		this.owner = owner;
	}
}