/*
 * Copyright (C) 2014 Ryan Michela
 * Copyright (C) 2016 Ronald Jack Jenkins Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ryanmichela.trees.rendering;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.ryanmichela.trees.history.WorldEditHistoryTracker;

public class WorldChangeTracker{

	private class Changer implements Runnable{
		private final WorldChange[] changes;
		private final int count;
		private final WorldEditHistoryTracker historyTracker;
		private final int offset;
		private final Location refPoint;

		private Changer(final WorldChange[] changes, final Location refPoint, final WorldEditHistoryTracker historyTracker, final int offset, final int count){
			this.changes = changes;
			this.refPoint = refPoint;
			this.historyTracker = historyTracker;
			this.offset = offset;
			this.count = count;
		}

		@Override public void run(){
			for(int i = offset; i < (offset + count); i++){
				final WorldChange change = changes[i];
				final Location changeLoc = refPoint.clone().add(change.location);
				final int blockY = changeLoc.getBlockY();
				ensureChunkLoaded(changeLoc.getChunk());
				if((blockY <= 255) && (blockY >= 0)) {
					if(historyTracker != null) {
						historyTracker.recordHistoricChange(changeLoc, change.blockData);
					}
				}
				
			}
		}

		private void ensureChunkLoaded(final Chunk chunk){
			if(!chunk.isLoaded() && !chunk.load()) {
				plugin.getLogger().severe("Could not load chunk " + chunk.toString());
			}
		}
	}

	private int BLOCKS_PER_TICK;
	private final Map<WorldChangeKey, WorldChange> changes = new HashMap<>(10000);
	private final Plugin plugin;

	private final boolean recordHistory;

	private int TICK_DELAY;

	public WorldChangeTracker(final Plugin plugin, final boolean recordHistory){
		this.plugin = plugin;
		this.recordHistory = recordHistory;

		BLOCKS_PER_TICK = plugin.getConfig().getInt("BLOCKS_PER_TICK", 2500);
		TICK_DELAY = plugin.getConfig().getInt("TICK_DELAY", 1);

		if(BLOCKS_PER_TICK < 1) {
			BLOCKS_PER_TICK = 1;
		}
		if(TICK_DELAY < 1) {
			TICK_DELAY = 1;
		}
	}

	public void addChange(final Vector location, final BlockData blockData, final boolean overwrite){
		addChange(new WorldChange(location, blockData), overwrite);
	}

	public void addChange(final WorldChange worldChange, final boolean overwrite){
		final WorldChangeKey key = new WorldChangeKey(worldChange.location.getBlockX(), worldChange.location.getBlockY(), worldChange.location.getBlockZ());
		if(changes.containsKey(key)) {
			if(overwrite) changes.put(key, worldChange);
		}
		else changes.put(key, worldChange);
	}

	public void applyChanges(final Location refPoint, final Player byPlayer){
		final WorldEditHistoryTracker historyTracker = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")
				? new WorldEditHistoryTracker(refPoint, byPlayer, recordHistory) : null;

		final WorldChange[] changesArray = changes.values().toArray(new WorldChange[changes.values().size()]);
		int i;
		for(i = 0; ((i + 1) * BLOCKS_PER_TICK) < changesArray.length; i++){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
					new Changer(changesArray, refPoint, historyTracker, i * BLOCKS_PER_TICK, BLOCKS_PER_TICK), i * TICK_DELAY);
		}

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
				new Changer(changesArray, refPoint, historyTracker, i * BLOCKS_PER_TICK, changesArray.length - (i * BLOCKS_PER_TICK)),
				(i + 1) * TICK_DELAY);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override public void run(){
				if(historyTracker != null) historyTracker.finalizeHistoricChanges();
			}
		}, (i + 2) * TICK_DELAY);

	}

	public WorldChange getChange(final WorldChangeKey key){
		return changes.get(key);
	}

	public Collection<WorldChange> getChanges(){
		return changes.values();
	}
}
