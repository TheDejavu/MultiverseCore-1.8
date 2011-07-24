package com.onarandombox.MultiverseCore.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.utils.Destination;
import com.onarandombox.utils.DestinationFactory;
import com.onarandombox.utils.InvalidDestination;

public class TeleportCommand extends MultiverseCommand {
    private MVTeleport playerTeleporter;

    public TeleportCommand(MultiverseCore plugin) {
        super(plugin);
        Permission self = new Permission("multiverse.core.tp.self", "Allows you to teleport yourself to other worlds.", PermissionDefault.OP);
        Permission other = new Permission("multiverse.core.tp.other", "Allows you to teleport yourself to other worlds.", PermissionDefault.OP);

        this.plugin.getServer().getPluginManager().addPermission(self);
        this.plugin.getServer().getPluginManager().addPermission(other);
        Map<String, Boolean> children = new HashMap<String, Boolean>();
        children.put(self.getName(), true);
        children.put(other.getName(), true);
        Permission alltp = new Permission("multiverse.core.tp.*", "Allows teleportation to other worlds.", PermissionDefault.OP, children);
        Permission tp = new Permission("multiverse.core.tp", "Allows teleportation to other worlds.", PermissionDefault.OP, children);
        this.plugin.getServer().getPluginManager().addPermission(alltp);
        this.setName("Teleport");
        this.setCommandUsage("/mvtp " + ChatColor.GOLD + "[PLAYER]" + ChatColor.GREEN + " {WORLD}");
        this.setArgRange(1, 2);
        this.addKey("mvtp");
        this.addKey("mv tp");
        this.setPermission(tp);
        this.playerTeleporter = new MVTeleport(this.plugin);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        // Check if the command was sent from a Player.
        Player teleporter = null;
        Player teleportee = null;
        if (sender instanceof Player) {
            teleporter = (Player) sender;
        }

        String worldName;

        if (args.size() == 2) {
            if (teleporter != null && !this.plugin.getPermissions().hasPermission(sender, "multiverse.core.tp.other", true)) {
                sender.sendMessage("You don't have permission to teleport another player. (multiverse.core.tp.other)");
                return;
            }
            teleportee = this.plugin.getServer().getPlayer(args.get(0));
            if (teleportee == null) {
                sender.sendMessage("Sorry, I couldn't find player: " + args.get(0));
                return;
            }
            worldName = args.get(1);

        } else {
            worldName = args.get(0);
            if (teleporter != null && !this.plugin.getPermissions().hasPermission(sender, "multiverse.core.tp.self", true)) {
                sender.sendMessage("You don't have permission to teleport yourself between worlds. (multiverse.core.tp.self)");
                return;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("From the console, you must specifiy a player to teleport");
                return;
            }
            teleporter = (Player) sender;
            teleportee = (Player) sender;
        }

        DestinationFactory df = this.plugin.getDestinationFactory();// .parseDestination(worldName, (MultiverseCore) this.plugin);
        Destination d = df.getDestination(worldName);
        if (d != null && d instanceof InvalidDestination) {
            sender.sendMessage("Multiverse does not know how to take you to: " + ChatColor.RED + worldName);
            return;
        }

        if (teleporter != null && !this.plugin.getPermissions().canEnterLocation(teleporter, d.getLocation())) {
            if (teleportee.equals(teleporter)) {
                teleporter.sendMessage("Doesn't look like you're allowed to go " + ChatColor.RED + "there...");
            } else {
                teleporter.sendMessage("Doesn't look like you're allowed to send " + ChatColor.GOLD + teleportee.getName() + ChatColor.WHITE + " to " + ChatColor.RED + "there...");
            }
            return;
        } else if (teleporter != null && !this.plugin.getPermissions().canTravelFromLocation(teleporter, d.getLocation())) {
            if (teleportee.equals(teleporter)) {
                teleporter.sendMessage("DOH! Doesn't look like you can get to " + ChatColor.RED + "THERE from " + ChatColor.GREEN + teleporter.getWorld().getName());
            } else {
                teleporter.sendMessage("DOH! Doesn't look like " + ChatColor.GREEN + teleporter.getWorld().getName() + " can get to " + ChatColor.RED + "THERE from where they are...");
            }
            return;
        }
        Location l = d.getLocation();
        if (l == null) {
            teleporter.sendMessage("Sorry Boss, I tried everything, but just couldn't teleport ya there!");
            return;
        }
        this.playerTeleporter.safelyTeleport(teleportee, l);
    }
}