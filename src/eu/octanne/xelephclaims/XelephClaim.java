package eu.octanne.xelephclaims;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class XelephClaim extends JavaPlugin{
	
	private static ClaimManager claimManager;
	private static Economy economy = null;
	
	static String pathFolder = "plugins/XelephClaims";
	static String pluginName = "XelephClaims";
	
	@Override
	public void onEnable() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
		claimManager = new ClaimManager();
		getCommand("claim").setExecutor(new ClaimCommand());
		getCommand("claim").setTabCompleter(new ClaimTabCompleter());
	}
		
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(claimManager.taskSave);
		for(ClaimOwner owner : getClaimsManager().getClaimOwners()) {
			ClaimOwner.save(owner);
		}
		//KICK PLAYER FOR RELOAD
		for(Player p : Bukkit.getOnlinePlayers()) p.kickPlayer("Rechargement du serveur !");
	}
	
	
	/*
	 * ITEMS CREATOR
	 */
	@SuppressWarnings("deprecation")
	static public ItemStack createItemStack(String DisplayName, Material id, int QteItem, ArrayList<String> Lore, int data, boolean Glowing){
				
		ItemStack item = new ItemStack(id, QteItem, (short) 0, (byte) data);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setLore(Lore);
		itemmeta.setDisplayName(DisplayName);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		if(Glowing){
			itemmeta.addEnchant(Enchantment.ARROW_FIRE, 10, true);
		}
		item.setItemMeta(itemmeta);
		return item;
	}		
	static public ItemStack createSkull(String DisplayName, ArrayList<String> Lore, SkullType Type, String Owner, boolean Glowing){
		
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) Type.ordinal());
		SkullMeta itemmeta = (SkullMeta) item.getItemMeta();
		itemmeta.setLore(Lore);
		itemmeta.setDisplayName(DisplayName);
		itemmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemmeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		itemmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		itemmeta.setOwner(Owner);
		if(Glowing){
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
