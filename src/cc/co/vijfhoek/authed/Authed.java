package cc.co.vijfhoek.authed;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class Authed extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");
	
	AuthedBlockListener blockListener;
	AuthedPlayerListener playerListener;
	
	public File fleConfigDir;
	public File fleAccounts;
	
	public Configuration cfgAccounts;
	
	public List<String> loggedInPlayers;
	public HashMap<String, HashMap<Integer, ItemStack>> stolenInventories = new HashMap<String, HashMap<Integer, ItemStack>>();
	
	public void onEnable() {
		loggedInPlayers = new ArrayList<String>();
		
		blockListener = new AuthedBlockListener();
		playerListener = new AuthedPlayerListener();
		
		PluginManager pm = getServer().getPluginManager();
		regEvents(pm);
		
		fleConfigDir = new File("plugins" + File.separator + "Authed");
		fleConfigDir.mkdirs();
		fleAccounts = new File("plugins" + File.separator + "Authed" + File.separator + "accounts.yml");
		
		cfgAccounts = new Configuration(fleAccounts);
		
		// Steal the inventories of registered users
		returnAllInventories();
		for (Player player : getServer().getOnlinePlayers()) {
			player.sendMessage(ChatColor.DARK_PURPLE + "Due to server maintenance, you will have");
			player.sendMessage(ChatColor.DARK_PURPLE + "to login again: /login <password>");
		}
	}
	
	public void onDisable() {
		// Return the inventories of loggedin users
		returnAllInventories();
		
		log.info("[Authed] Disabled");
	}
	
	private void regEvents(PluginManager pm) {
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
		
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Highest, this);
		
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] cmdArgs) {
		if (!(sender instanceof Player)) {
			// We don't want console commands in this plugin
			return false;
		}
		
		// Place the information in convenient variables
		Player player = (Player)sender;
		String name = player.getName();
		
		if (cmdLabel.equalsIgnoreCase("login")) {
			// Wrong syntax
			if (cmdArgs.length != 1) return false;
			
			// Load the account file
			cfgAccounts.load();
			
			if (cfgAccounts.getNode(player.getName()) != null) {
				// Get the hash of the password
				int passCfg = cfgAccounts.getInt(name + ".password", 0);
				
				// If there is no password, don't continue
				if (passCfg == 0) return true;
				
				// Check the password
				if (passCfg == cmdArgs[0].hashCode()) {
					// Add the user to loggedInPlayers
					loggedInPlayers.add(player.getName());
					
					// Put back the inventory
					returnInventory(player);
					
					// Say it to the users
					player.sendMessage(ChatColor.GREEN + "Logged in successfully");
				} else {
					// Say the user that his password is wrong
					player.sendMessage(ChatColor.RED + "Wrong password");
				}
			}
			return true;
		} else if (cmdLabel.equalsIgnoreCase("register")) {
			if (cmdArgs.length != 1) return false;
			cfgAccounts.load();
			
			// Check whether the user already has an account
			if (cfgAccounts.getNode(player.getName()) != null) {
				player.sendMessage(ChatColor.RED + "You already have an account");
				return true;
			}
			
			cfgAccounts.setProperty(name + ".password", cmdArgs[0].hashCode());
			cfgAccounts.setProperty(name + ".restrict.break", true);
			cfgAccounts.setProperty(name + ".restrict.place", true);
			cfgAccounts.save();
			
			player.sendMessage(ChatColor.GREEN + "Created account successfully");
			player.sendMessage(ChatColor.GREEN + "Log in now: /login <password>");
			return true;
		}
		return false;
	}
	
	public void stealInventory(Player player) { 
		String name = player.getName();
		if (cfgAccounts.getNode(name) != null) {
			// Put the player's inventory in stolenInventory
			HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
			for (int i = 0; i < 36; i++) {
				ItemStack item = player.getInventory().getItem(i);
				if (item.getAmount() == 0) continue;
				items.put(i, item);
			}
			stolenInventories.put(name, items);
			
			// Clear the player's inventory
			player.getInventory().clear();
		}
	}
	
	public void returnInventory(Player player) {
		String name = player.getName();
		if (stolenInventories.containsKey(name)) {
			// Clear the player's inventory
			player.getInventory().clear();
			
			// Retrieve the inventory from stolenInventory
			HashMap<Integer, ItemStack> items = stolenInventories.get(name);
			
			// Put the inventory back
			for (int i = 0; i < 36; i++) {
				if (!items.containsKey(i)) continue;
				ItemStack item = items.get(i);
				
				if (item.getAmount() == 0) continue;
				player.getInventory().setItem(i, item);
			}
			
			// Remove the inventory from stolenInventory
			stolenInventories.remove(name);
		}
	}
	
	public void stealAllInventories() {
		for (Player player : getServer().getOnlinePlayers()) {
			stealInventory(player);
		}
	}
	
	public void returnAllInventories() {
		for (Player player : getServer().getOnlinePlayers()) {
			returnInventory(player);
		}
	}
}
