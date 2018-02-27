package party.lemons.enderhopper;

import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Sam on 26/02/2018.
 */
public class BlockEnderHopper extends BlockHopper
{
	public BlockEnderHopper()
	{
		super();

		this.setRegistryName(EnderHopper.MODID, "enderhopper");
		this.setUnlocalizedName(EnderHopper.MODID + ":enderhopper");
		this.setHardness(1.4F);
		this.setResistance(4F);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityEnderHopper();
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntityEnderHopper tileentity = (TileEntityEnderHopper) worldIn.getTileEntity(pos);

		if (stack.hasDisplayName())
		{
			if (tileentity instanceof TileEntityHopper)
			{
				tileentity.setCustomName(stack.getDisplayName());
			}
		}

		if(placer instanceof EntityPlayer)
		{
			tileentity.setOwner(placer.getUniqueID());
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (worldIn.isRemote)
		{
			return true;
		}
		else
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if (tileentity instanceof TileEntityHopper)
			{
				playerIn.openGui(EnderHopper.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
				playerIn.addStat(StatList.HOPPER_INSPECTED);
			}

			return true;
		}
	}
}
