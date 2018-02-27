package party.lemons.enderhopper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;

/**
 * Created by Sam on 26/02/2018.
 */
public class ContainerEnderHopper extends ContainerHopper
{
	public ContainerEnderHopper(InventoryPlayer playerInventory, IInventory hopperInventoryIn, EntityPlayer player)
	{
		super(playerInventory, hopperInventoryIn, player);
	}
}
