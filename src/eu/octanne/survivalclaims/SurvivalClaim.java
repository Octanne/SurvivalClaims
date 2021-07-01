package eu.octanne.survivalclaims;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class SurvivalClaim extends JavaPlugin{
	
	private static ClaimManager claimManager;
	private static Economy economy = null;
	
	
	static String pathFolder = "plugins/SurvivalClaims";
	static String pluginName = "SurvivalClaims";
	
	@Override
	public void onEnable() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
		claimManager = new ClaimManager();
		
		getCommand("claim").setExecutor(new ClaimCommand());
		getCommand("claim").setTabCompleter(new ClaimTabCompleter());
		claimManager.loadClaims();
	}
		
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(claimManager.taskSave);
		for(ClaimOwner owner : getClaimsManager().getClaimOwners()) {
			ClaimOwner.save(owner);
		}
		
	}
	
	
	/*
	 * ITEMS CREATOR
	 */
	// CREATE ITEM WITH DATA
	static public ItemStack createItemStack(String DisplayName, Material id, int QteItem, ArrayList<String> Lore,
			boolean Glowing, boolean unbreakable) {

		ItemStack item = new ItemStack(id, QteItem);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(DisplayName);
		itemmeta.setLore(Lore);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		if (Glowing) {
			itemmeta.addEnchant(Enchantment.ARROW_FIRE, 10, true);
		}
		itemmeta.setUnbreakable(unbreakable);
		item.setItemMeta(itemmeta);
		return item;
	}

	static public ItemStack createItemStack(String DisplayName, Material id, int QteItem, ArrayList<String> Lore,
			ItemMeta meta, boolean unbreakable) {

		ItemStack item = new ItemStack(id, QteItem);
		ItemMeta itemmeta = meta;
		itemmeta.setDisplayName(DisplayName);
		itemmeta.setLore(Lore);
		itemmeta.setUnbreakable(unbreakable);
		item.setItemMeta(itemmeta);
		return item;
	}
	
	static public ItemStack createItemSkull(String DisplayName, ArrayList<String> Lore, UUID Owner,
			boolean Glowing) {

		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta itemmeta = (SkullMeta) item.getItemMeta();
		itemmeta.setLore(Lore);
		itemmeta.setDisplayName(DisplayName);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		itemmeta.setOwningPlayer(Bukkit.getOfflinePlayer(Owner));
		if (Glowing) {
			itemmeta.addEnchant(Enchantment.DURABILITY, 10, true);
		}
		item.setItemMeta(itemmeta);
		return item;
	}
	
	/*
	 * GETTERS
	 */
	static public ClaimManager getClaimsManager() {return claimManager;}
	public static Plugin getInstance() {
		return Bukkit.getPluginManager().getPlugin(pluginName);
	}
	static public Economy getEconomy() {
		return economy;
	}
}
