package party.lemons.enderhopper;

import net.minecraft.client.gui.GuiHopper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Created by Sam on 26/02/2018.
 */
public class GuiProxy implements IGuiHandler
{
	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y, z);
		TileEntityEnderHopper te = (TileEntityEnderHopper) world.getTileEntity(pos);

		return new ContainerEnderHopper(player.inventory, te, player);
	}

	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		BlockPos pos = new BlockPos(x, y, z);
		TileEntityEnderHopper te = (TileEntityEnderHopper) world.getTileEntity(pos);
		return new GuiHopper(player.inventory, te);
	}
}
