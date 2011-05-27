package cc.co.vijfhoek.authed;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AuthedBlockListener extends BlockListener {
	public void onBlockBreak (BlockBreakEvent event) {
		// Initialize Authed class
		Authed authed = new Authed();
		
		String name = event.getPlayer().getName();
		Player player = event.getPlayer();
		
		authed.cfgAccounts.load();
		
		try {
			if (!authed.loggedInPlayers.contains(name)) {
				if (authed.cfgAccounts.getNode(name) != null) {
					player.sendMessage(ChatColor.RED + "Please log in first: /login <password>");
					event.setCancelled(true);
				}
			}
		} catch (Exception e) {}
	}
	
	public void onBlockPlace (BlockPlaceEvent event) {
		// Initialize Authed class
		Authed authed = new Authed();
		
		String name = event.getPlayer().getName();
		Player player = event.getPlayer();
		
		authed.cfgAccounts.load();
		
		try {
			if (!authed.loggedInPlayers.contains(name)) {
				if (authed.cfgAccounts.getNode(name) != null) {
					player.sendMessage(ChatColor.RED + "Please log in first: /login <password>");
					event.setCancelled(true);
				}
			}
		} catch (Exception e) {}
	}
}
