package cc.co.vijfhoek.authed;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class AuthedPlayerListener extends PlayerListener {
	public void onPlayerJoin (PlayerJoinEvent event) {
		// Initialize Authed class
		Authed authed = new Authed();
		
		// Place information in convenient variables
		Player player = event.getPlayer();
		String name = player.getName();
		
		// Make sure the name isn't in loggedInPlayers
		authed.loggedInPlayers.remove(name);
		
		// Steal the inventory!
		authed.stealInventory(player);
		
		// Send the login message
		player.sendMessage(ChatColor.DARK_PURPLE + "Please log in: /login <password>");
	}
	
	public void onPlayerQuit (PlayerQuitEvent event) {
		// Initizalize Authed class
		Authed authed = new Authed();
		
		// Place information in convenient variables
		Player player = event.getPlayer();
		String name = player.getName();

		// Remove the name from loggedInPlayers
		authed.loggedInPlayers.remove(name);
		
		// Put back the inventory
		if (authed.stolenInventory.containsKey(name)) {
			HashMap<Integer, ItemStack> items = authed.stolenInventory.get(name);
			for (int i = 0; i < 36; i++) {
				if (!items.containsKey(i)) continue;
				ItemStack item = items.get(i);
				if (item.getAmount() == 0) continue;
				try { player.getInventory().setItem(i, item); } catch (Exception ex) {}
			}
			authed.stolenInventory.remove(name);
		}
	}
	
	public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
		// Initialize Authed class
		Authed authed = new Authed();
		
		// Place information in convenient variables
		Player player = event.getPlayer();
		String name = player.getName();
		
		// Block all commands from users that aren't logged in
		if (!(event.getMessage().startsWith("/login")) && !(authed.loggedInPlayers.contains(event.getPlayer().getName()))) {
			if (authed.cfgAccounts.getNode(name) != null) {
				event.getPlayer().sendMessage(ChatColor.RED + "Please log in first: /login <password>");
				event.setCancelled(true);
			}
		}
	}
}