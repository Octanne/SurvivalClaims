package eu.octanne.xelephclaims;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import eu.octanne.xelephclaims.util.AnvilGUI;
import eu.octanne.xelephclaims.util.AnvilGUI.AnvilSlot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ClaimManager implements Listener{
	
	protected ArrayList<ClaimOwner> ownerClaim = new ArrayList<ClaimOwner>();
	
	private BossBar barFlyEnable = Bukkit.createBossBar("§aActivation du Fly...", BarColor.GREEN, BarStyle.SOLID);
	private BossBar barFlyDisable = Bukkit.createBossBar("§cDésactivation du Fly...", BarColor.RED, BarStyle.SOLID);
	
	private ArrayList<String> playerMap = new ArrayList<String>();
	private ArrayList<Integer> amountPriceClaims;
	private int cooldownSave = 1799;
	
	public int taskSave;
	
	@SuppressWarnings("unchecked")
	public ClaimManager() {
		File worldFile = new File(XelephClaim.pathFolder+"/config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
		if(!worldFile.exists()) {
			config.set("worlds", new ArrayList<String>());
			ArrayList<Integer> amountPrice = new ArrayList<Integer>();
			amountPrice.add(10);
			amountPrice.add(20);
			amountPrice.add(30);
			amountPrice.add(40);
			amountPriceClaims = amountPrice;
			config.set("prices", amountPrice);
			try {
				config.save(worldFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File dataFolder = new File(XelephClaim.pathFolder+"/data");
		if(!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		amountPriceClaims = (ArrayList<Integer>) config.get("prices");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(XelephClaim.getInstance(), new Runnable() {

			@Override
			public void run() {
				for(String playerName: playerMap) {
					Bukkit.getPlayer(playerName).sendMessage(getMap(Bukkit.getPlayer(playerName)));
				}
			}
			
		}, 40, 40);
		taskSave = Bukkit.getScheduler().scheduleSyncRepeatingTask(XelephClaim.getInstance(), new Runnable() {

			@Override
			public void run() {
				if(cooldownSave == 1800) {
					System.out.println("[XelephClaims] Lancement de la sauvegarde des ClaimOwner...");
					for(ClaimOwner owner : getClaimOwners()) {
						ClaimOwner.save(owner);
					}
					System.out.println("[XelephClaims] Sauvegarde des ClaimOwner terminé !");
					cooldownSave=0;
				}else {
					cooldownSave++;
				}
			}
		}, 20, 20);
		Bukkit.getPluginManager().registerEvents(this, XelephClaim.getInstance());
	}
	
	/*
	 * MAP SYSTEM
	 */
	public String getStatutMap(Player p) {
		if(playerMap.contains(p.getName())) {
			return "§aActivé";
		}else return "§cDésactivé";
	}
	public String[] getMap(Player p) {
		String[] map = {"§6§l§m========§r§lMap§6§l§m========", 
						" --------------------- ", 
						" --------------------- ", 
						" --------------------- ", 
					 	" ----------§9O§r---------- ",
				        " --------------------- ", 
				        " --------------------- ", 
				        " --------------------- ",
				        "§6§l§m========§r§lMap§6§l§m========"};
		Chunk chunk = Bukkit.getWorld(p.getWorld().getName()).getChunkAt(p.getLocation().getChunk().getX()+3, p.getLocation().getChunk().getZ()-10);
		
		for(int lign = 1; lign <= 7; lign++) {
			map[lign] = "";
			int alphaX = lign-1;
			map[lign] = map[lign]+"§6§l|§r";
			for(int column = 1; column <= 21; column++) {
				int alphaZ = column-1;
				ClaimChunk claim = new ClaimChunk(chunk);
				claim.setZ(claim.getZ()+alphaZ);
				claim.setX(claim.getX()-alphaX);
				//System.out.println(claim.getSaveFormat());
				if(this.isClaim(claim.getChunk())) {
					if(new ClaimChunk(p.getLocation().getChunk()).equals(claim)) {
						map[lign] = map[lign]+"§9O§r";
					}else if(this.getClaimOwner(claim.getChunk()).getOwner().equals(p.getName())){
						map[lign] = map[lign]+"§aX§r";
					}else {
						map[lign] = map[lign]+"§cX§r";
					}
				}else {
					if(new ClaimChunk(p.getLocation().getChunk()).equals(claim)) {
						map[lign] = map[lign]+"§9O§r";
					}else {
						map[lign] = map[lign]+"-";
					}
				}
			}
			map[lign] = map[lign]+"§6§l|§r";
			if(lign == 5) {
				map[lign] = map[lign]+" Ta position: §9O§r";
			}
			if(lign == 6) {
				map[lign] = map[lign]+" Tes Claims: §aX§r";
			}
			if(lign == 7) {
				map[lign] = map[lign]+" Zone Claim: §cX§r";
			}
			
			if(lign == 1) {
				map[lign] = map[lign]+"          §l▲";
			}
			if(lign == 2) {
				map[lign] = map[lign]+" Nord §l<  >§r Sud";
			}
			if(lign == 3) {
				map[lign] = map[lign]+"          §l▼";
			}
		}
		return map;
	}
	public void closeMap(Player player) {
		if(playerMap.contains(player.getName())) {
			playerMap.remove(player.getName());
		}
	}
	public void openMap(Player player) {
		if(!playerMap.contains(player.getName())) {
			playerMap.add(player.getName());
		}
	}
	
	/*
	 * CLAIM & UNCLAIM
	 */
	public boolean claim(Player p) {
		for(ClaimOwner cp : ownerClaim) {
			if(cp.hasClaim(p.getLocation().getChunk())) return false;
		}
		getClaimOwner(p.getName()).addClaim(p.getLocation().getChunk());
		return true;
	}
	public boolean claim(Player p, Chunk chunk) {
		for(ClaimOwner cp : ownerClaim) {
			if(cp.hasClaim(chunk)) return false;
		}
		getClaimOwner(p.getName()).addClaim(chunk);
		return true;
	}
	public boolean claim(Player p, ClaimChunk chunk) {
		for(ClaimOwner cp : ownerClaim) {
			if(cp.hasClaim(chunk.getChunk())) return false;
		}
		getClaimOwner(p.getName()).addClaim(chunk.getChunk());
		return true;
	}
	public boolean unclaim(Player p) {
		for(ClaimOwner cp : ownerClaim) {
			if(cp.hasClaim(p.getLocation().getChunk())) {
				cp.removeClaim(p.getLocation().getChunk());
				return true;
			}
		}
		return false;
	}
	
	/*
	 * ACTIONS
	 */
	private void flyMessage(String statut, Player p) {
		barFlyDisable.removePlayer(p);
		barFlyEnable.removePlayer(p);
		if(statut.equalsIgnoreCase("on")) {
			barFlyEnable.addPlayer(p);
			Bukkit.getScheduler().scheduleSyncDelayedTask(XelephClaim.getInstance(), new Runnable() {

				@Override
				public void run() {
					barFlyEnable.removePlayer(p);
				}
				
			}, 30);
		}else if(statut.equalsIgnoreCase("off")){
			barFlyDisable.addPlayer(p);
			Bukkit.getScheduler().scheduleSyncDelayedTask(XelephClaim.getInstance(), new Runnable() {

				@Override
				public void run() {
					barFlyDisable.removePlayer(p);
				}
				
			}, 30);
		}
	}
	
	/*
	 * MENU FRIENDS
	 */
	private void openAddFriendMenu(Player p) {
		AnvilGUI menu = new AnvilGUI(p, new AnvilGUI.AnvilClickEventHandler() {
			
			@Override
			public void onAnvilClick(AnvilGUI.AnvilClickEvent event) {
				event.setWillClose(false);
				event.setWillDestroy(false);
				if(event.getSlot() == AnvilGUI.AnvilSlot.OUTPUT) {
					if(!XelephClaim.getClaimsManager().getClaimOwner(event.getPlayer().getName()).getFriends().contains(event.getName()) && !event.getName().equals(event.getPlayer().getName())) {
						XelephClaim.getClaimsManager().getClaimOwner(event.getPlayer().getName()).addFriend(event.getName());
						event.getPlayer().sendMessage(ChatColor.GREEN+"Validation: "+ChatColor.BLUE+event.getName().toString()+ChatColor.GREEN+" viens d'être ajouté votre liste d'ami.");
						openFriendMenu(event.getPlayer());
						event.setWillDestroy(true);
					}else if(XelephClaim.getClaimsManager().getClaimOwner(event.getPlayer().getName()).getFriends().contains(event.getName())){
						event.getPlayer().sendMessage(ChatColor.RED+"Erreur: "+ChatColor.BLUE+event.getName().toString()+ChatColor.RED+" est déjà dans votre liste d'ami.");
						openFriendMenu(event.getPlayer());
						event.setWillDestroy(true);
					}else {
						event.getPlayer().sendMessage(ChatColor.RED+"Erreur: Vous ne pouvez pas vous ajouter dans votre liste d'ami.");
						openFriendMenu(event.getPlayer());
						event.setWillDestroy(true);
					}
				}
			}
		});
		//FRIEND ITEM
		ItemStack addFriendItem;
		ArrayList<String> loreADD = new ArrayList<String>();
		loreADD.add("§aAjouter un(e) ami(e):");
		loreADD.add("§7Remplacer par le nom du joueur");
		loreADD.add("§7et renommer l'item.");
		addFriendItem = XelephClaim.createItemStack("Pseudo", Material.PAPER, 1, loreADD, 0, true);
		menu.setSlot(AnvilSlot.INPUT_LEFT, addFriendItem);
		try {
			menu.open();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	public boolean openFriendMenu(Player p, int page) {
		Inventory friends = Bukkit.createInventory(null, 27, "§6Xeleph§cClaim - Amis");

		int pageT = 0;
		int nbrO = 0;
		for(int nbr = 1; nbrO <= getClaimOwner(p.getName()).getFriends().size(); nbr++) {
			nbrO = 18*nbr;
		}
		pageT = nbrO/18;

		//SET OFFER
		if(getClaimOwner(p.getName()).getFriends().size() != 0) {	
			int numOffer = 18*page-18;
			int numSlot = 0;
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.RED+"Shift-Click");
			lore.add(ChatColor.GRAY+"Pour retirer des amis");
			while(numOffer < getClaimOwner(p.getName()).getFriends().size()){
				if(friends.getItem(17) == null) {
					friends.setItem(numSlot, XelephClaim.createSkull(getClaimOwner(p.getName()).getFriends().get(numOffer), lore, SkullType.PLAYER, getClaimOwner(p.getName()).getFriends().get(numOffer), false));
					numSlot++;
					numOffer++;
				}else {
					break;
				}
			}
		}

		//NAVIGATE BAR
		//Page suivante
		ArrayList<String> lorePS = new ArrayList<String>();
		lorePS.add(ChatColor.GRAY+"Page: "+ page +"/"+ pageT);
		lorePS.add(ChatColor.GRAY+"Page actuelle : "+ page);
		friends.setItem(26, XelephClaim.createItemStack(ChatColor.GRAY+"Page Suivante ", Material.MAP, 1, lorePS, 0, false));
		//Page prec
		ArrayList<String> lorePP = new ArrayList<String>();
		lorePP.add(ChatColor.GRAY+"Page: "+ page +"/"+ pageT);
		lorePP.add(ChatColor.GRAY+"Page actuelle : "+ page);
		friends.setItem(18, XelephClaim.createItemStack(ChatColor.GRAY+"Page Précédente ", Material.MAP, 1, lorePP, 0, false));
		//RETURN
		ArrayList<String> loreR = new ArrayList<String>();
		lorePP.add(ChatColor.GRAY+"Retour au menu");
		friends.setItem(19, XelephClaim.createItemStack(ChatColor.RED+"Retour", Material.ARROW, 1, loreR, 0, false));
		//ADD FRIEND
		ArrayList<String> loreAddF = new ArrayList<String>();
		loreAddF.add(ChatColor.GREEN+"Ajouter un(e) ami(e)");
		friends.setItem(22, XelephClaim.createItemStack(ChatColor.BLUE+"Ajouter ami(e)", Material.PAPER, 1, loreR, 0, false));
		//INFO FRIEND
		ArrayList<String> loreInfo = new ArrayList<String>();
		loreInfo.add(ChatColor.GRAY+"Les commandes:");
		loreInfo.add(ChatColor.GREEN+"Ajouter un(e) ami(e)");
		loreInfo.add(ChatColor.GREEN+"- /claim addfriend <joueur>");
		loreInfo.add(ChatColor.RED+"Supprimer un(e) ami(e)");
		loreInfo.add(ChatColor.RED+"- /claim removefriend <joueur>");
		friends.setItem(25, XelephClaim.createItemStack(ChatColor.BLUE+"Informations", Material.REDSTONE_TORCH_ON, 1, loreInfo, 0, false));
		p.openInventory(friends);
		return true;
	}
	public boolean openFriendMenu(Player p) {
		int page = 1;
		Inventory friends = Bukkit.createInventory(null, 27, "§6Xeleph§cClaim - Amis");

		int pageT = 0;
		int nbrO = 0;
		for(int nbr = 1; nbrO <= getClaimOwner(p.getName()).getFriends().size(); nbr++) {
			nbrO = 18*nbr;
		}
		pageT = nbrO/18;

		//SET OFFER
		if(getClaimOwner(p.getName()).getFriends().size() != 0) {	
			int numOffer = 0;
			int numSlot = 0;
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.RED+"Shift-Click");
			lore.add(ChatColor.GRAY+"Pour retirer des amis");
			while(numOffer < getClaimOwner(p.getName()).getFriends().size()){
				if(friends.getItem(17) == null) {
					friends.setItem(numSlot, XelephClaim.createSkull(getClaimOwner(p.getName()).getFriends().get(numOffer), lore, SkullType.PLAYER, getClaimOwner(p.getName()).getFriends().get(numOffer), false));
					numSlot++;
					numOffer++;
				}else {
					break;
				}
			}
		}

		//NAVIGATE BAR
		//Page suivante
		ArrayList<String> lorePS = new ArrayList<String>();
		lorePS.add(ChatColor.GRAY+"Page: "+ page +"/"+ pageT);
		lorePS.add(ChatColor.GRAY+"Page actuelle : "+ page);
		friends.setItem(26, XelephClaim.createItemStack(ChatColor.GRAY+"Page Suivante ", Material.MAP, 1, lorePS, 0, false));
		//Page prec
		ArrayList<String> lorePP = new ArrayList<String>();
		lorePP.add(ChatColor.GRAY+"Page: "+ page +"/"+ pageT);
		lorePP.add(ChatColor.GRAY+"Page actuelle : "+ page);
		friends.setItem(18, XelephClaim.createItemStack(ChatColor.GRAY+"Page Précédente ", Material.MAP, 1, lorePP, 0, false));
		//RETURN
		ArrayList<String> loreR = new ArrayList<String>();
		lorePP.add(ChatColor.GRAY+"Retour au menu");
		friends.setItem(19, XelephClaim.createItemStack(ChatColor.RED+"Retour", Material.ARROW, 1, loreR, 0, false));
		//ADD FRIEND
		ArrayList<String> loreAddF = new ArrayList<String>();
		loreAddF.add(ChatColor.GREEN+"Ajouter un(e) ami(e)");
		friends.setItem(22, XelephClaim.createItemStack(ChatColor.BLUE+"Ajouter ami(e)", Material.PAPER, 1, loreR, 0, false));
		//INFO FRIEND
		ArrayList<String> loreInfo = new ArrayList<String>();
		loreInfo.add(ChatColor.GRAY+"Les commandes:");
		loreInfo.add(ChatColor.GREEN+"Ajouter un(e) ami(e)");
		loreInfo.add(ChatColor.GREEN+"- /claim addfriend <joueur>");
		loreInfo.add(ChatColor.RED+"Supprimer un(e) ami(e)");
		loreInfo.add(ChatColor.RED+"- /claim removefriend <joueur>");
		friends.setItem(25, XelephClaim.createItemStack(ChatColor.BLUE+"Informations", Material.REDSTONE_TORCH_ON, 1, loreInfo, 0, false));
		p.openInventory(friends);
		return true;
	}
	
	/*
	 * MENU MAIN
	 */
	@SuppressWarnings("deprecation")
	public boolean openMenu(Player player) {
		Inventory inv = Bukkit.createInventory(null, 27, "§6Xeleph§cClaim");
		
		//HEAD
		ArrayList<String> loreInfo = new ArrayList<String>();
		loreInfo.add(ChatColor.RED+"Nom: "+ChatColor.BLUE+""+player.getName());
		double money = XelephClaim.getEconomy().getBalance(player.getName());
		BigDecimal bd = new BigDecimal(money).setScale(2, RoundingMode.HALF_EVEN);
		loreInfo.add(ChatColor.RED+"Argent: "+ChatColor.BLUE+""+bd.doubleValue());
		loreInfo.add(ChatColor.RED+"Nb Claim: "+ChatColor.BLUE+""+this.getClaimOwner(player.getName()).getClaims().size());
		ItemStack infoHead = XelephClaim.createSkull(ChatColor.GRAY+"Informations", loreInfo, SkullType.PLAYER, player.getName(), false);
		inv.setItem(0, infoHead);
		
		//FRIEND
		ArrayList<String> loreFriend = new ArrayList<String>();
		loreFriend.add(ChatColor.GRAY+"Clique pour ouvrir le menu");
		ItemStack infoFriend = XelephClaim.createItemStack(ChatColor.BLUE+"Ajouter ami(e)", Material.NETHER_STAR, 1, loreFriend, 0, true);
		inv.setItem(18, infoFriend);
		
		//CLOSE
		ArrayList<String> loreClose = new ArrayList<String>();
		ItemStack closeBarrier = XelephClaim.createItemStack(ChatColor.RED+"Fermer", Material.BARRIER, 1, loreClose, 0, false);
		inv.setItem(26, closeBarrier);
		
		//MAP
		ArrayList<String> loreMap = new ArrayList<String>();
		loreMap.add("§7Statut: "+this.getStatutMap(player));
		ItemStack mapItem = XelephClaim.createItemStack(ChatColor.BLUE+"Map", Material.MAP, 1, loreMap, 0, false);
		inv.setItem(19, mapItem);
		
		//CLAIMS
		ArrayList<String> loreClaimsDirt = new ArrayList<String>();
		loreClaimsDirt.add("§aPrix: "+amountPriceClaims.toArray()[0]+"£");
		loreClaimsDirt.add("§cTaille: 1 Chunk");
		ItemStack claimDirt = XelephClaim.createItemStack(ChatColor.BLUE+"Claim (16X16)", Material.DIRT, 1, loreClaimsDirt, 0, false);
		inv.setItem(12, claimDirt);
		
		ArrayList<String> loreClaimsGrass = new ArrayList<String>();
		loreClaimsGrass.add("§aPrix: "+amountPriceClaims.toArray()[1]+"£");
		loreClaimsGrass.add("§cTaille: 2 Chunks X 2 Chunks");
		ItemStack claimGrass = XelephClaim.createItemStack(ChatColor.BLUE+"Claim (32X32)", Material.GRASS, 1, loreClaimsGrass, 0, false);
		inv.setItem(13, claimGrass);
		
		ArrayList<String> loreClaimsStone = new ArrayList<String>();
		loreClaimsStone.add("§aPrix: "+amountPriceClaims.toArray()[2]+"£");
		loreClaimsStone.add("§cTaille: 3 Chunks X 3 Chunks");
		ItemStack claimStone = XelephClaim.createItemStack(ChatColor.BLUE+"Claim (48X48)", Material.STONE, 1, loreClaimsStone, 0, false);
		inv.setItem(14, claimStone);
		
		//UNCLAIM
		ArrayList<String> loreUnclaim =new ArrayList<String>();
		loreUnclaim.add("§bUnclaim un de vos chunks");
		ItemStack unclaim = XelephClaim.createItemStack(ChatColor.BLUE+"Unclaim (16X16)", Material.MYCEL, 1, loreUnclaim, 0, true);
		inv.setItem(8, unclaim);
		
		if(player.hasPermission("instantclaims.admin")) {
			ArrayList<String> loreClaimsAdmin = new ArrayList<String>();
			loreClaimsAdmin.add("§aPrix: "+amountPriceClaims.toArray()[3]+"£");
			loreClaimsAdmin.add("§cTaille: 5 Chunks X 5 Chunks");
			ItemStack claimAdmin = XelephClaim.createItemStack(ChatColor.RED+"Claim (80X80)", Material.GOLD_BLOCK, 1, loreClaimsAdmin, 0, false);
			inv.setItem(22, claimAdmin);
			
			ArrayList<String> loreUnclaimAdmin = new ArrayList<String>();
			loreUnclaimAdmin.add("§bUnclaim un chunk");
			loreUnclaimAdmin.add("§cTaille: 1 Chunk");
			ItemStack unclaimAdmin = XelephClaim.createItemStack(ChatColor.RED+"Unclaim (16X16)", Material.MYCEL, 1, loreUnclaimAdmin, 0, true);
			inv.setItem(8, unclaimAdmin);
		}
		
		player.openInventory(inv);
		return true;
	}
	
	/*
	 * CONDITIONS
	 */
	public boolean isClaim(Chunk chunk) {
		for(ClaimOwner claimO : getClaimOwners()) {
			if(claimO.hasClaim(chunk)) {
				return true;
			}
		}
		return false;
	}
	public boolean isClaim(ClaimChunk chunk) {
		for(ClaimOwner claimO : getClaimOwners()) {
			if(claimO.hasClaim(chunk.getChunk())) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * GETTERS
	 */
	public ClaimOwner getClaimOwner(Chunk chunk) {
		if(!ownerClaim.isEmpty()) {
			for(ClaimOwner claimO : ownerClaim) {
				if(claimO.hasClaim(chunk)) {
					return claimO;
				}
			}
			return new ClaimOwner("Zone-Libre", OwnerType.REGION);
		}else return new ClaimOwner("Zone-Libre", OwnerType.REGION);

	}
	public ClaimOwner getClaimOwner(String ownerName) {
		if(!ownerClaim.isEmpty()) {
			for(ClaimOwner claimO : ownerClaim) {
				if(claimO.getOwner().equals(ownerName)) {
					return claimO;
				}
			}
			return ClaimOwner.get(ownerName);
		}else return ClaimOwner.get(ownerName);

	}
	public ArrayList<ClaimOwner> getClaimOwners(){return ownerClaim;}
	public File[] getFileOwners(){
		File file = new File(XelephClaim.pathFolder+"/data/");
		return file.listFiles();
	}
	public ArrayList<ClaimOwner> getAllClaimOwners(){
		ArrayList<ClaimOwner> claimOs = new ArrayList<ClaimOwner>();
		for(File file : getFileOwners()) {
			claimOs.add(ClaimOwner.get(file.getName()));
		}
		return claimOs;
	}
	public ArrayList<Integer> getPrices() {return this.amountPriceClaims;}
	
	/*
	 * EVENTS BUKKIT
	 * FOR LOAD/UNLOAD OWNER
	 */
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		//IS IT ALREADY LOAD ?
		if(!ownerClaim.isEmpty()) {
			for(ClaimOwner claimO : ownerClaim) {
				if(claimO.hasClaim(e.getChunk())) {
					System.out.println(claimO.owner+" et déjà chargé");
					return;
				}
			}
		}
		//LOAD THE GOOD OWNER
		for(File file : getFileOwners()) {
			ClaimOwner owner = ClaimOwner.get(file.getName());
			//IS IT THE GOOD OWNER ?
			if(owner.hasClaim(e.getChunk())) {
				ownerClaim.add(owner);
				System.out.println("Chargement de "+ClaimOwner.get(file.getName()).owner);
				return;
			}
		}
	}
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		//IS IT ALREADY LOAD ?
		if(!ownerClaim.isEmpty()) {
			for(ClaimOwner claimO : ownerClaim) {
				if(claimO.hasClaim(e.getChunk())) {
					//HAS IT A OTHER CHUNK LOAD ?
					for(ClaimChunk cChunk : claimO.getClaims()) {
						if(cChunk.getChunk().isLoaded()) {
							System.out.println(claimO.owner+" à encors des chunks chargé");
							return;
						}
					}
					//IS HE A PLAYER & CONNECTED ?
					if(claimO.isPlayer() && Bukkit.getPlayerExact(claimO.getOwner()) != null) {
						return;
					}else{
						ClaimOwner.save(claimO);
						ownerClaim.remove(claimO);
						System.out.println("Déchargement de "+claimO.owner);
						return;
					}
				}
			}
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		//FLY
		if(isClaim(e.getPlayer().getLocation().getChunk())) {
			if(this.getClaimOwner(e.getPlayer().getLocation().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getPlayer().getLocation().getChunk()).getOwner().equals(e.getPlayer().getName())) {
				if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
					e.getPlayer().setAllowFlight(true);
					flyMessage("on", e.getPlayer());
				}
			}
		}
		//IS HE ALREADY LOAD ?
		if(!ownerClaim.isEmpty()) {
			for(ClaimOwner claimO : ownerClaim) {
				if(claimO.isPlayer() && claimO.getOwner().equals(e.getPlayer().getName())) {
					System.out.println(claimO.owner+" et déjà chargé");
					return;
				}
			}
		}
		//IF HE EXIST ALREADY
		if(ClaimOwner.get(e.getPlayer().getName()) != null) {
			ownerClaim.add(ClaimOwner.get(e.getPlayer().getName()));
			System.out.println("Chargement de "+ClaimOwner.get(e.getPlayer().getName()).owner);
		}else {
			ClaimOwner owner = new ClaimOwner(e.getPlayer().getName(), OwnerType.PLAYER);
			ClaimOwner.save(owner);
			ownerClaim.add(owner);
			System.out.println("Création de "+owner.owner);
		}
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		//System.out.println("Player Quit");
		if(this.playerMap.contains(e.getPlayer().getName())) {this.playerMap.remove(e.getPlayer().getName());}
		if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR))e.getPlayer().setAllowFlight(false);
		//GET GOOD OWNER
		/*if(ownerClaim.size() > 0) {
			for(ClaimOwner claimO : ownerClaim) {
				if(claimO.isPlayer() && claimO.getOwner().equals(e.getPlayer().getName())) {
					//HAS HE A OTHER CHUNK LOAD ?
					for(ClaimChunk cChunk : claimO.getClaims()) {
						if(cChunk.getChunk().isLoaded()) {
							System.out.println(claimO.owner+" à encors des chunks chargé");
							return;
						}
					}
					ClaimOwner.save(claimO);
					ownerClaim.remove(claimO);
					System.out.println("Déchargement de "+claimO.owner);
				}
			}
		}*/
	}
	
	/*
	 * EVENTS BUKKIT
	 * FOR CANCEL INTERACT & AREA CHANGE
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
			if(this.isClaim(e.getTo().getChunk())) {
				if(this.isClaim(e.getFrom().getChunk()) &&
					!this.getClaimOwner(e.getFrom().getChunk()).getOwner().equals("Zone-Libre") &&	
					!this.getClaimOwner(e.getFrom().getChunk()).equals(this.getClaimOwner(e.getTo().getChunk()))) {
					//FLY MODE
					if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getAllowFlight() && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
						if(this.getClaimOwner(e.getTo().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getTo().getChunk()).getOwner().equals(e.getPlayer().getName())) {
							e.getPlayer().setAllowFlight(true);
							flyMessage("on", e.getPlayer());
						}else if(!e.getPlayer().hasPermission("essentials.fly") && e.getPlayer().getAllowFlight()){
							e.getPlayer().setAllowFlight(false);
							flyMessage("off", e.getPlayer());
						}
					}
					//SEND IN ACTIONBAR
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GRAY+"Propriété de "+ChatColor.RED+""+this.getClaimOwner(e.getTo().getChunk()).getOwner()));
					return;
				}
				if(!this.isClaim(e.getFrom().getChunk())) {
					//FLY MODE
					if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getAllowFlight() && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
						if(this.getClaimOwner(e.getTo().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getTo().getChunk()).getOwner().equals(e.getPlayer().getName())) {
							e.getPlayer().setAllowFlight(true);
							flyMessage("on", e.getPlayer());
						}else if(!e.getPlayer().hasPermission("essentials.fly") && e.getPlayer().getAllowFlight()){
							e.getPlayer().setAllowFlight(false);
							flyMessage("off", e.getPlayer());
						}
					}
					//SEND IN ACTIONBAR
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GRAY+"Propriété de "+ChatColor.RED+""+this.getClaimOwner(e.getTo().getChunk()).getOwner()));
					return;
				}
			}else if(!this.isClaim(e.getTo().getChunk()) && this.isClaim(e.getFrom().getChunk())){
				if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
					if(!e.getPlayer().hasPermission("essentials.fly") && e.getPlayer().getAllowFlight()){
						e.getPlayer().setAllowFlight(false);
						flyMessage("off", e.getPlayer());
					}
				}
				//SEND IN ACTIONBAR
				e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN+"Zone Libre"));
			}
		}
	}
	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent e) {
		if(this.isClaim(e.getBlock().getChunk())) {
			if(this.getClaimOwner(e.getBlock().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getBlock().getChunk()).getOwner().equals(e.getPlayer().getName())) {
				
			}else {
				if(!e.getPlayer().hasPermission("instantclaims.claims.bypass")) {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"§c⚠ Vous n'êtes pas autorisé ⚠ (Zone protégé par "+this.getClaimOwner(e.getBlock().getChunk()).getOwner()+")"));
					e.setCancelled(true);
				}else {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c⚠ Vous êtes entrain de Bypass ⚠ (Zone protégé par "+this.getClaimOwner(e.getBlock().getChunk()).getOwner()+")"));
				}
			}
		}
	}
	@EventHandler
	public void onBreakBlock(BlockBreakEvent e) {
		if(this.isClaim(e.getBlock().getChunk())) {
			if(this.getClaimOwner(e.getBlock().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getBlock().getChunk()).getOwner().equals(e.getPlayer().getName())) {
				
			}else {
				if(!e.getPlayer().hasPermission("instantclaims.claims.bypass")) {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"§c⚠ Vous n'êtes pas autorisé ⚠ (Zone protégé par "+this.getClaimOwner(e.getBlock().getChunk()).getOwner()+")"));
					e.setCancelled(true);
				}else {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c⚠ Vous êtes entrain de Bypass ⚠ (Zone protégé par "+this.getClaimOwner(e.getBlock().getChunk()).getOwner()+")"));
				}
			}
		}
	}
	@EventHandler
	public void onInteractEntity(PlayerInteractAtEntityEvent e) {
		if(this.isClaim(e.getRightClicked().getLocation().getChunk())) {
			if(this.getClaimOwner(e.getRightClicked().getLocation().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getRightClicked().getLocation().getChunk()).getOwner().equals(e.getPlayer().getName())) {
				
			}else {
				if(!e.getPlayer().hasPermission("instantclaims.claims.bypass")) {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"§c⚠ Vous n'êtes pas autorisé ⚠ (Zone protégé par "+this.getClaimOwner(e.getRightClicked().getLocation().getChunk()).getOwner()+")"));
					e.setCancelled(true);
				}else {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c⚠ Vous êtes entrain de Bypass ⚠ (Zone protégé par "+this.getClaimOwner(e.getRightClicked().getLocation().getChunk()).getOwner()+")"));
				}
			}
		}
	}
	@EventHandler
	public void onInteractBlock(PlayerInteractEvent e) {
		if(e.getClickedBlock() != null && this.isClaim(e.getClickedBlock().getChunk()) && !(e.getClickedBlock().getType().equals(Material.WORKBENCH) || e.getClickedBlock().getType().equals(Material.ENDER_CHEST))) {
			if(this.getClaimOwner(e.getClickedBlock().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getClickedBlock().getChunk()).getOwner().equals(e.getPlayer().getName())) {
				
			}else {
				if(!e.getPlayer().hasPermission("instantclaims.claims.bypass")) {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"§c⚠ Vous n'êtes pas autorisé ⚠ (Zone protégé par "+this.getClaimOwner(e.getClickedBlock().getChunk()).getOwner()+")"));
					e.setCancelled(true);
				}else {
					//MESSAGE
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c⚠ Vous êtes entrain de Bypass ⚠ (Zone protégé par "+this.getClaimOwner(e.getClickedBlock().getChunk()).getOwner()+")"));
				}
			}
		}
	}
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		if(!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
			if(this.isClaim(e.getTo().getChunk())) {
				if(this.isClaim(e.getFrom().getChunk()) &&
					!this.getClaimOwner(e.getFrom().getChunk()).getOwner().equals("Zone-Libre") &&	
					!this.getClaimOwner(e.getFrom().getChunk()).equals(this.getClaimOwner(e.getTo().getChunk()))) {
					//FLY MODE
					if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getAllowFlight() && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
						if(this.getClaimOwner(e.getTo().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getTo().getChunk()).getOwner().equals(e.getPlayer().getName())) {
							e.getPlayer().setAllowFlight(true);
							flyMessage("on", e.getPlayer());
						}else if(!e.getPlayer().hasPermission("essentials.fly") && e.getPlayer().getAllowFlight()){
							e.getPlayer().setAllowFlight(false);
							flyMessage("off", e.getPlayer());
						}
					}
					//SEND IN ACTIONBAR
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GRAY+"Propriété de "+ChatColor.RED+""+this.getClaimOwner(e.getTo().getChunk()).getOwner()));
					return;
				}
				if(!this.isClaim(e.getFrom().getChunk())) {
					//FLY MODE
					if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getAllowFlight() && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
						if(this.getClaimOwner(e.getTo().getChunk()).isFriend(e.getPlayer().getName()) || this.getClaimOwner(e.getTo().getChunk()).getOwner().equals(e.getPlayer().getName())) {
							e.getPlayer().setAllowFlight(true);
							flyMessage("on", e.getPlayer());
						}else if(!e.getPlayer().hasPermission("essentials.fly") && e.getPlayer().getAllowFlight()){
							e.getPlayer().setAllowFlight(false);
							flyMessage("off", e.getPlayer());
						}
					}
					//SEND IN ACTIONBAR
					e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GRAY+"Propriété de "+ChatColor.RED+""+this.getClaimOwner(e.getTo().getChunk()).getOwner()));
					return;
				}
			}else if(!this.isClaim(e.getTo().getChunk()) && this.isClaim(e.getFrom().getChunk())){
				if(e.getPlayer().hasPermission("xelephclaims.fly") && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && !e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
					if(!e.getPlayer().hasPermission("essentials.fly") && e.getPlayer().getAllowFlight()){
						e.getPlayer().setAllowFlight(false);
						flyMessage("off", e.getPlayer());
					}
				}
				//SEND IN ACTIONBAR
				e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN+"Zone Libre"));
			}
		}
	}
	
	/*
	 * EVENTS BUKKIT
	 * FOR MENU MAIN & FRIENDS ACTION
	 */
	@EventHandler
	public void onDragInventory(InventoryDragEvent e) {
		if(e.getInventory() != null && e.getInventory().getName().equals("§6Xeleph§cClaim")) {
			e.setCancelled(true);
		}
		if(e.getInventory() != null && e.getInventory().getName().equals("§6Xeleph§cClaim - Amis")) {
			e.setCancelled(true);
		}
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClickInventory(InventoryClickEvent e) {
		if(e.getClickedInventory() != null && e.getClickedInventory().getName().equals("§6Xeleph§cClaim - Amis")) {
			e.setCancelled(true);
			//PAGE SUIVANTE
			if(e.getCurrentItem().equals(e.getInventory().getItem(26)) && e.getInventory().getItem(26).hasItemMeta() && e.getInventory().getItem(26).getItemMeta().hasDisplayName()) {
				String StrNumPage = ""+e.getCurrentItem().getItemMeta().getLore().get(1).substring(18);
				int numPage = Integer.parseInt(StrNumPage);
				String StrNumPageT[] = e.getCurrentItem().getItemMeta().getLore().get(0).split("/");
				int numPageT = Integer.parseInt(StrNumPageT[1]);
				if(numPageT == numPage) {
					return;
				}else {
					this.openFriendMenu((Player)e.getWhoClicked(), numPage+1);
				}
			}
			//PAGE PRECEDENTE
			else if(e.getCurrentItem().equals(e.getInventory().getItem(18)) && e.getInventory().getItem(18).hasItemMeta() && e.getInventory().getItem(18).getItemMeta().hasDisplayName()) {
				String StrNumPage = ""+e.getCurrentItem().getItemMeta().getLore().get(1).substring(18);
				int numPage = Integer.parseInt(StrNumPage);
				if(numPage == 1) {
					return;
				}else {
					this.openFriendMenu((Player)e.getWhoClicked(), numPage-1);
				}
			}
			//ADD FRIEND
			else if(e.getCurrentItem().equals(e.getInventory().getItem(22)) && e.getInventory().getItem(22).hasItemMeta() && e.getInventory().getItem(22).getItemMeta().hasDisplayName()) {
				openAddFriendMenu((Player)e.getWhoClicked());
			}
			//RETOUR
			else if(e.getCurrentItem().equals(e.getInventory().getItem(19)) && e.getInventory().getItem(19).hasItemMeta() && e.getInventory().getItem(19).getItemMeta().hasDisplayName()) {
				this.openMenu((Player) e.getWhoClicked());
			}else {
				if(e.getClick().isShiftClick()) {
					String StrNumPage = ""+e.getInventory().getItem(18).getItemMeta().getLore().get(1).substring(18);
					int numPage = Integer.parseInt(StrNumPage);
					ItemStack item = e.getCurrentItem();
					this.getClaimOwner(e.getWhoClicked().getName()).removeFriend(item.getItemMeta().getDisplayName());
					e.getWhoClicked().sendMessage(ChatColor.GREEN+"Validation: "+ChatColor.BLUE+item.getItemMeta().getDisplayName().toString()+ChatColor.GREEN+" viens d'être supprimé de votre liste d'ami.");
					this.openFriendMenu((Player) e.getWhoClicked(), numPage);
				}
			}
		}
		if(e.getClickedInventory() != null && e.getClickedInventory().getName().equals("§6Xeleph§cClaim")) {
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.BARRIER)) {
				e.getWhoClicked().closeInventory();
				((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1.0F, 1.0F);
			}
			//FRIENDS
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.NETHER_STAR)) {
				this.openFriendMenu((Player) e.getWhoClicked());
			}
			//MAP
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.MAP)) {
				if(this.getStatutMap((Player) e.getWhoClicked()).startsWith("§cDésa")) {
					this.openMap((Player) e.getWhoClicked());
				}else this.closeMap((Player) e.getWhoClicked());
				this.openMenu((Player) e.getWhoClicked());
			}
			//UNCLAIM
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.MYCEL)) {
				if(this.isClaim(e.getWhoClicked().getLocation().getChunk()) && (e.getWhoClicked().getName().equals(this.getClaimOwner(e.getWhoClicked().getLocation().getChunk()).getOwner()) || e.getWhoClicked().hasPermission("instantclaims.unclaim.bypass"))) {
					this.unclaim((Player) e.getWhoClicked());
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
					e.getWhoClicked().sendMessage("§aCe chunk viens d'être libéré.");
					e.getWhoClicked().closeInventory();
				}else if(!e.getWhoClicked().getName().equals(this.getClaimOwner(e.getWhoClicked().getLocation().getChunk()).getOwner()) 
						&& this.isClaim(e.getWhoClicked().getLocation().getChunk())){
					e.getWhoClicked().sendMessage("§cErreur: Ce chunk ne vous appartient pas.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
					e.getWhoClicked().closeInventory();
				}else {
					e.getWhoClicked().sendMessage("§cErreur: Ce chunk n'appartient à personne.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
					e.getWhoClicked().closeInventory();
				}
				
			}
			//CLAIM 16X16
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.DIRT)) {
				e.getWhoClicked().closeInventory();
				if(!this.isClaim(e.getWhoClicked().getLocation().getChunk()) && XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())>= this.getPrices().get(0)) {
					//PRELEVEMENT ARGENT
					XelephClaim.getEconomy().withdrawPlayer(e.getWhoClicked().getName(), XelephClaim.getClaimsManager().getPrices().get(0));
					//CLAIM
					this.claim((Player) e.getWhoClicked());
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§aValidation: Protection zone de 16X16 pour §b"+this.getPrices().toArray()[0]+"£.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
				}else if(this.isClaim(e.getWhoClicked().getLocation().getChunk())){
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Cette zone appartient déjà à quelqu'un.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}else if(XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName()) < XelephClaim.getClaimsManager().getPrices().get(0)) {
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Vous n'avez pas l'argent necessaire.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}
			}
			//CLAIM 32X32
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.GRASS)) {
				e.getWhoClicked().closeInventory();
				if(!this.isClaim(e.getWhoClicked().getLocation().getChunk()) && 
						!this.isClaim(new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+(e.getWhoClicked().getLocation().getChunk().getX()+1)+"/"+e.getWhoClicked().getLocation().getChunk().getZ())) &&
						!this.isClaim(new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+e.getWhoClicked().getLocation().getChunk().getX()+"/"+(e.getWhoClicked().getLocation().getChunk().getZ()-1))) &&
						!this.isClaim(new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+(e.getWhoClicked().getLocation().getChunk().getX()+1)+"/"+(e.getWhoClicked().getLocation().getChunk().getZ()-1))) &&
						XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())>= this.getPrices().get(1)) {
					//PRELEVEMENT ARGENT
					XelephClaim.getEconomy().withdrawPlayer(e.getWhoClicked().getName(), this.getPrices().get(1));
					//CLAIM
					this.claim((Player) e.getWhoClicked());
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+(e.getWhoClicked().getLocation().getChunk().getX()+1)+"/"+e.getWhoClicked().getLocation().getChunk().getZ()));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+e.getWhoClicked().getLocation().getChunk().getX()+"/"+(e.getWhoClicked().getLocation().getChunk().getZ()-1)));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+(e.getWhoClicked().getLocation().getChunk().getX()+1)+"/"+(e.getWhoClicked().getLocation().getChunk().getZ()-1)));
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§aValidation: Protection zone de 32X32 pour §b"+this.getPrices().get(1)+"£.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
				}else if(this.isClaim(e.getWhoClicked().getLocation().getChunk()) ||
						this.isClaim(new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+(e.getWhoClicked().getLocation().getChunk().getX()+1)+"/"+e.getWhoClicked().getLocation().getChunk().getZ())) ||
						this.isClaim(new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+e.getWhoClicked().getLocation().getChunk().getX()+"/"+(e.getWhoClicked().getLocation().getChunk().getZ()-1))) ||
						this.isClaim(new ClaimChunk(e.getWhoClicked().getWorld().getName()+"/"+(e.getWhoClicked().getLocation().getChunk().getX()+1)+"/"+(e.getWhoClicked().getLocation().getChunk().getZ()-1)))){
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Cette zone appartient déjà à quelqu'un.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}else if(XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())< this.getPrices().get(1)) {
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Vous n'avez pas l'argent necessaire.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}
			}
			//CLAIM 48X48
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.STONE)) {
				e.getWhoClicked().closeInventory();
				Chunk chunk = e.getWhoClicked().getLocation().getChunk();
				if(!this.isClaim(e.getWhoClicked().getLocation().getChunk()) && XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())>= this.getPrices().get(2)
					&&	!this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ()+1 )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ() )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ()-1 )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX() )+"/"+(chunk.getZ()+1 )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX() )+"/"+(chunk.getZ()-1 )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ()+1 )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ() )))
				&& !this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ()-1 )))) {
					//PRELEVEMENT ARGENT
					XelephClaim.getEconomy().withdrawPlayer(e.getWhoClicked().getName(), this.getPrices().get(2));
					//CLAIM
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ()+1 )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ() )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ()-1 )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX() )+"/"+(chunk.getZ()+1 )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX() )+"/"+(chunk.getZ()-1 )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ()+1 )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ() )));
					this.claim((Player) e.getWhoClicked(), new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ()-1 )));
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§aValidation: Protection zone de 48X48 pour §b"+this.getPrices().get(2)+"£.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
				}else if(this.isClaim(e.getWhoClicked().getLocation().getChunk())
					||	this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ()+1 )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ() )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()-1 )+"/"+(chunk.getZ()-1 )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX() )+"/"+(chunk.getZ()+1 )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX() )+"/"+(chunk.getZ()-1 )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ()+1 )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ() )))
						|| this.isClaim(new ClaimChunk(chunk.getWorld().getName()+"/"+(chunk.getX()+1 )+"/"+(chunk.getZ()-1 )))){
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Cette zone appartient déjà à quelqu'un.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}else if(XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())< this.getPrices().get(2)) {
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Vous n'avez pas l'argent necessaire.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}
			}
			//CLAIM 80X80
			if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.GOLD_BLOCK)) {
				e.getWhoClicked().closeInventory();
				if(!this.isClaim(e.getWhoClicked().getLocation().getChunk()) && XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())>= this.getPrices().get(3)) {
					//PRELEVEMENT ARGENT
					//XelephClaim.getEconomy().withdrawPlayer(e.getWhoClicked().getName(), this.getPrices().get(3));
					//CLAIM
					e.getWhoClicked().sendMessage("§cErreur: non dispo pour le moment.");
					
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§aValidation: Protection zone de 80X80 pour §b"+this.getPrices().get(3)+"£.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
				}else if(this.isClaim(e.getWhoClicked().getLocation().getChunk())){
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Cette zone appartient déjà à quelqu'un.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}else if(XelephClaim.getEconomy().getBalance(e.getWhoClicked().getName())< this.getPrices().get(3)) {
					//MESSAGE & SOUND
					e.getWhoClicked().sendMessage("§cErreur: Vous n'avez pas l'argent necessaire.");
					((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ENDERMEN_HURT, 1.0F, 1.0F);
				}
			}
			e.setCancelled(true);
		}
	}	
}
