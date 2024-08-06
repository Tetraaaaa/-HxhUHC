package fr.nathan.plugin.utils;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

public class VisibilityUtil {

    public static void setPlayerInvisible(Player player, boolean invisible) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            CraftPlayer craftOnlinePlayer = (CraftPlayer) onlinePlayer;
            CraftPlayer craftPlayer = (CraftPlayer) player;

            if (invisible) {
                // Hide player
                craftOnlinePlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, craftPlayer.getHandle()));
                craftOnlinePlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(player.getEntityId()));
            } else {
                // Show player
                showPlayer(craftOnlinePlayer, craftPlayer);
            }
        }
    }

    public static void showPlayer(CraftPlayer recipient, CraftPlayer target) {
        recipient.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, target.getHandle()));
        recipient.getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(target.getHandle()));
        sendArmorPackets(recipient.getPlayer(), target.getPlayer());
    }

    private static void sendArmorPackets(Player recipient, Player target) {
        EntityEquipment equipment = target.getEquipment();

        // Helmet
        sendEquipmentPacket(recipient, target, 4, equipment.getHelmet());
        // Chestplate
        sendEquipmentPacket(recipient, target, 3, equipment.getChestplate());
        // Leggings
        sendEquipmentPacket(recipient, target, 2, equipment.getLeggings());
        // Boots
        sendEquipmentPacket(recipient, target, 1, equipment.getBoots());
    }

    private static void sendEquipmentPacket(Player recipient, Player target, int slot, org.bukkit.inventory.ItemStack item) {
        if (item == null) return;

        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(target.getEntityId(), slot, CraftItemStack.asNMSCopy(item));
        ((CraftPlayer) recipient).getHandle().playerConnection.sendPacket(packet);
    }
}
