package eu.octanne.survivalclaims;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class ClaimChunk {
	
	private int x, z;
	private String world;
	
	public ClaimChunk(Chunk chunk) {
		this.x = chunk.getX();
		this.z = chunk.getZ();
		this.world = chunk.getWorld().getName();
	}
	
	public ClaimChunk(int x, int z, String world) {
		this.x = x;
		this.z = z;
		this.world = world;
	}
	
	protected ClaimChunk(String serialize) {
		ClaimChunk deserialize = ClaimChunk.deserialize(serialize);
		this.world = deserialize.getWorld();
		this.x = deserialize.getX();
		this.z = deserialize.getZ();
	}
	
	/*
	 * GETTERS
	 */
	public int getX() {return x;}
	public int getZ() {return z;}
	public String getWorld() {return world;}
	public Chunk getChunk() {
		return Bukkit.getWorld(world).getChunkAt(x, z);
	}
	
	/*
	 * SETTERS
	 */
	public void setX(int x) {this.x=x;}
	public void setZ(int z) {this.z=z;}
	
	/*
	 * SERIALIZE
	 */
	public String serialize() {
		return world+"/"+x+"/"+z;
	}
	
	/*
	 * CONDITIONS
	 */
	public boolean equals(ClaimChunk claimChunk) {
		if(claimChunk.getX() == this.x && claimChunk.getZ() == this.z && claimChunk.getWorld().equals(this.world)) {
			return true;
		}else return false;
	}
	public boolean equals(Chunk chunk) {
		if(chunk.getX() == this.x && chunk.getZ() == this.z && chunk.getWorld().getName().equals(this.world)) {
			return true;
		}else return false;
	}
	
	/**
	 * DESERIALIZE
	 * @param serializeFormat
	 * @return ClaimChunk
	 */
	static public ClaimChunk deserialize(String serializeFormat) {
		String world = serializeFormat.split("/")[0];
		int x = Integer.parseInt(serializeFormat.split("/")[1]), z = Integer.parseInt(serializeFormat.split("/")[2]);
		return new ClaimChunk(x, z, world);
	}
		 
}
