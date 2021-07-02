package eu.octanne.survivalclaims;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ClaimTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;

			if(args.length == 1) {
				List<String> autoCompleter = new ArrayList<>();
				autoCompleter.add("map");
				autoCompleter.add("addfriend");
				autoCompleter.add("removefriend");
				return autoCompleter;
			}
			if(args.length == 2) {
				List<String> autoCompleter = new ArrayList<>();
				if(args[0].equalsIgnoreCase("addfriend")) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p != player) {
							autoCompleter.add(p.getName());
						}
					}
					return autoCompleter;
				}else if(args[0].equalsIgnoreCase("removefriend")){
					for(UUID friendId : ClaimOwner.get(player.getUniqueId()).friends) {
						autoCompleter.add(Bukkit.getOfflinePlayer(friendId).getName());
					}
					return autoCompleter;
				} else {
					return autoCompleter;
				}
			}
		}

		List<String> autoCompleter = new ArrayList<>();
		return autoCompleter;
	}
}
