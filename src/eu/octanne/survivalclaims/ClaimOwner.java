package eu.octanne.survivalclaims;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;

public class ClaimOwner {
	
	protected ArrayList<UUID> friends;
	protected ArrayList<ClaimChunk> claim;
	protected UUID owner;
	protected OwnerType type;
	
	public ClaimOwner(UUID ownerId, OwnerType type){
		owner = ownerId;
		this.type = type;
		this.claim = new ArrayList<ClaimChunk>();
		this.friends = new ArrayList<UUID>();
 	}
	
	private ClaimOwner(UUID ownerId, OwnerType type, ArrayList<ClaimChunk> claim, ArrayList<UUID> friends) {
		owner = ownerId;
		this.type = type;
		this.claim = claim;
		if(friends == null) {friends = new ArrayList<UUID>();} 
		this.friends = friends;
	}
	
	/*
	 * CLAIMS GESTION
	 */
	public boolean addClaim(Chunk chunk) {
		for(ClaimChunk chunkc : claim) {
			if(chunkc.getChunk().equals(chunk)) {
				return false;
			}
		}
		claim.add(new ClaimChunk(chunk));
		return true;
	}
	public boolean removeClaim(Chunk chunk) {
		for(ClaimChunk chunkc : claim) {
			if(chunkc.getChunk().equals(chunk)) {
				claim.remove(chunkc);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * FRIENDS GESTION
	 */
	public boolean addFriend(UUID playerId) {
		if(!friends.contains(playerId)) {
			friends.add(playerId);
			return true;
		}else return false;
	}
	public boolean removeFriend(UUID playerId) {
		if(friends.contains(playerId)) {
			friends.remove(playerId);
			return true;
		}else return false;
	}
	
	/*
	 * GETTERS
	 */
	public UUID getOwner() {return owner;}
	public OwnerType getType() {return type;}
	public ArrayList<ClaimChunk> getClaims() {return claim;}
	public ArrayList<UUID> getFriends(){return friends;}
	
	/*
	 * CONDITIONS
	 */
	public boolean isPlayer() {
		if(type.equals(OwnerType.PLAYER)) {
			return true;
		}else return false;
	}
	public boolean equals(ClaimOwner owner) {
		if(owner.getOwner().equals(this.owner)) {
			return true;
		}else return false;
	}
	public boolean hasClaim(Chunk chunk) {
		for(ClaimChunk claim : claim) {
			if(claim.equals(chunk)) {
				return true;
			}
		}
		return false;
	}
	public boolean isFriend(UUID playerId) {
		if(friends.contains(playerId)) {
			return true;
		}else return false;
	}
	
	/*
	 * GET A ClaimOwner
	 */
	@SuppressWarnings("unchecked")
	static ClaimOwner get(UUID ownerId) {
		ArrayList<ClaimChunk> claims = new ArrayList<ClaimChunk>();
		OwnerType type;
		File file = new File(SurvivalClaim.pathFolder+"/data/"+ownerId);
		
		if(file.exists()) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			ArrayList<String> serializeClaims  = (ArrayList<String>) config.get("Claims");
			for(String serializeClaim : serializeClaims) {claims.add(ClaimChunk.deserialize(serializeClaim));}
			if(config.getString("type").equalsIgnoreCase("player")) {
				type = OwnerType.PLAYER;
			} else type = OwnerType.REGION;
			return new ClaimOwner(ownerId, type, claims, (ArrayList<UUID>) config.get("Friends"));
		}else return null;
	}
	/*
	 * SAVE a ClaimOwner
	 */
	static boolean save(ClaimOwner owner) {
		ArrayList<String> claimsSerialize = new ArrayList<String>();
		File file = new File(SurvivalClaim.pathFolder+"/data/"+owner.getOwner());
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		for(ClaimChunk chunk : owner.getClaims()) {claimsSerialize.add(chunk.serialize());}
		config.set("type", owner.getType().toString());
		config.set("Claims", claimsSerialize);
		config.set("Friends", owner.getFriends());
		try {
			config.save(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
}
