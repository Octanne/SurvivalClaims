package eu.octanne.survivalclaims;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class ClaimCommand implements CommandExecutor{

	File worldFile = new File(SurvivalClaim.pathFolder+"/config.yml");
	YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
	
	@SuppressWarnings({"unchecked", "deprecation"})
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player && ((ArrayList<String>)config.get("worlds")).contains(((Player) sender).getWorld().getName())) {
			Player player = (Player) sender;
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("addfriend")) {
					if(args.length >= 2) {
						Player friend = Bukkit.getPlayerExact(args[1]);
						if(friend == null) {
							player.sendMessage(ChatColor.RED+"Erreur: Ce joueur n'existe pas ou il n'est pas connecté.");
						}else if(!SurvivalClaim.getClaimsManager().getClaimOwner(player.getUniqueId()).getFriends().contains(friend.getUniqueId())) {
							SurvivalClaim.getClaimsManager().getClaimOwner(player.getUniqueId()).addFriend(friend.getUniqueId());
							player.sendMessage(ChatColor.GREEN+"Validation: "+ChatColor.BLUE+args[1].toString()+ChatColor.GREEN+" viens d'être ajouté votre liste d'ami.");
						}else if(SurvivalClaim.getClaimsManager().getClaimOwner(player.getUniqueId()).getFriends().contains(friend.getUniqueId())){
							player.sendMessage(ChatColor.RED+"Erreur: "+ChatColor.BLUE+args[1].toString()+ChatColor.RED+" est déjà dans votre liste d'ami.");
						}else {
							player.sendMessage(ChatColor.RED+"Erreur: Vous ne pouvez pas vous ajouter dans votre liste d'ami.");
						}
					}else {
						player.sendMessage(ChatColor.RED+"Usage: /claim addfriend <joueur> - ajouter un(e) ami(e)");
					}
				}
				else if(args[0].equalsIgnoreCase("removefriend")) {
					if(args.length >= 2) {
						OfflinePlayer friend = Bukkit.getOfflinePlayer(args[1]);
						if(friend == null) {
							player.sendMessage(ChatColor.RED+"Erreur: Ce joueur n'existe pas ou il n'est pas connecté.");
						}else if(SurvivalClaim.getClaimsManager().getClaimOwner(player.getUniqueId()).getFriends().contains(friend.getUniqueId())) {
							SurvivalClaim.getClaimsManager().getClaimOwner(player.getUniqueId()).removeFriend(friend.getUniqueId());
							player.sendMessage(ChatColor.GREEN+"Validation: "+ChatColor.BLUE+args[1].toString()+ChatColor.GREEN+" viens d'être supprimé de votre liste d'ami.");
						}else {
							player.sendMessage(ChatColor.RED+"Erreur: "+ChatColor.BLUE+args[1].toString()+ChatColor.RED+" n'est pas dans votre liste d'ami.");
						}
					}else {
						player.sendMessage(ChatColor.RED+"Usage: /claim removefriend <joueur> - Supprimer un(e) ami(e)");
					}
				}else if(args[0].equalsIgnoreCase("map")) {
					player.sendMessage(SurvivalClaim.getClaimsManager().getMap((Player) player));
				}else {
					SurvivalClaim.getClaimsManager().openMenu((Player) player);
				}
			}else {
				SurvivalClaim.getClaimsManager().openMenu((Player) player);
			}
			return true;
		}else {
			sender.sendMessage(ChatColor.RED+"Cette commande '/claim' n'est pas disponible ici.");
			return true;
		}
	}

}
