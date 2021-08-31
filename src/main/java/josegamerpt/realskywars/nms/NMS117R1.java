package josegamerpt.realskywars.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.locale.LocaleLanguage;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.entity.TileEntityChest;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMS117R1 implements RSWnms {

    @Override
    public void chestAnimation(Chest chest, boolean open) {
        Location location = chest.getLocation();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityChest ec = (TileEntityChest) world.getTileEntity(position);
        assert (ec != null);
        world.playBlockAction(position, world.getType(position).getBlock(), 1, open ? 1 : 0);
    }

    @Override
    public String getItemName(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        return LocaleLanguage.a().a(nmsStack.getItem().getName());
    }
}
