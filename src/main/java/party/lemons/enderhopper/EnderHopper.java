package party.lemons.enderhopper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Sam on 26/02/2018.
 */
@Mod(modid = EnderHopper.MODID, name = EnderHopper.NAME, version = EnderHopper.VERSION)
@Mod.EventBusSubscriber
public class EnderHopper
{
	public static final String MODID = "enderhopper";
	public static final String NAME = "Ender Hopper";
	public static final String VERSION = "1.0.1";

	@Mod.Instance(MODID)
	public static EnderHopper INSTANCE;

	@ObjectHolder("enderhopper:enderhopper")
	public static Block ENDER_HOPPER = Blocks.AIR;

	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().register(new BlockEnderHopper());
		GameRegistry.registerTileEntity(TileEntityEnderHopper.class, MODID + ":enderhopper");
	}

	@SubscribeEvent
	public static void	onItemRegister(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().register(
				new ItemBlock(ENDER_HOPPER).setRegistryName("enderhopper")
		);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onModelRegister(ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ENDER_HOPPER), 0, new ModelResourceLocation(ENDER_HOPPER.getRegistryName(), "inventory"));

		ModelLoader.setCustomStateMapper(
				ENDER_HOPPER, (new StateMap.Builder()).ignore(BlockHopper.ENABLED).build()
		);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent e)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiProxy());
	}
}
