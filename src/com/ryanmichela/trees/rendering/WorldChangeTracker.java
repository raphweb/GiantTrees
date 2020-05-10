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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.ryanmichela.trees.history.WorldEditHistoryTracker;

public class WorldChangeTracker{

	private class Changer implements Runnable{
		private final List<WorldChange> changes;
		private final WorldEditHistoryTracker historyTracker;
		private final Location refPoint;

		private Changer(final List<WorldChange> changes, final Location refPoint, final WorldEditHistoryTracker historyTracker){
			this.changes = changes;
			this.refPoint = refPoint;
			this.historyTracker = historyTracker;
		}

		@Override public void run(){
			this.changes.forEach(change -> {
				final Location changeLoc = refPoint.clone().add(change.location);
				final int blockY = changeLoc.getBlockY();
				ensureChunkLoaded(changeLoc.getChunk());
				if((blockY <= 255) && (blockY >= 0)) {
					if(historyTracker != null) {
						historyTracker.recordHistoricChange(changeLoc, change.blockData);
					} else {
						// TODO: apply change without world edit
					}
				}
				
			});
		}

		private void ensureChunkLoaded(final Chunk chunk){
			if(!chunk.isLoaded() && !chunk.load()) {
				plugin.getLogger().severe("Could not load chunk " + chunk.toString());
			}
		}
	}

	private final int BLOCKS_PER_TICK;
	private final Map<BlockVector, WorldChange> changes = new HashMap<>(10000);
	private final Plugin plugin;

	private final boolean recordHistory;

	private final int TICK_DELAY;

	public WorldChangeTracker(final Plugin plugin, final boolean recordHistory){
		this.plugin = plugin;
		this.recordHistory = recordHistory;

		this.BLOCKS_PER_TICK = Math.max(1, plugin.getConfig().getInt("BLOCKS_PER_TICK", 2500));
		this.TICK_DELAY = Math.max(1, plugin.getConfig().getInt("TICK_DELAY", 1));
	}

	public void addChange(final Vector location, final BlockData blockData, final boolean overwrite){
		addChange(new WorldChange(location, blockData), overwrite);
	}

	public void addChange(final WorldChange worldChange, final boolean overwrite){
		// manually create block vector, the Vector#toBlockVector implementation does not floor the values but rather casts them
		// integer casting has different results than flooring for negative and positive numbers
		final BlockVector key = new BlockVector(worldChange.location.getBlockX(), worldChange.location.getBlockY(),
				worldChange.location.getBlockZ());
		if(!changes.containsKey(key) || overwrite) {
			changes.put(key, worldChange);
		}
	}

	public void applyChanges(final Location refPoint, final Player byPlayer){
		final WorldEditHistoryTracker historyTracker = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")
				? new WorldEditHistoryTracker(refPoint, byPlayer, recordHistory) : null;

		final AtomicInteger counter = new AtomicInteger();

		changes.values().stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / BLOCKS_PER_TICK)).forEach((i, l) ->
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Changer(l, refPoint, historyTracker), i * TICK_DELAY));

		counter.set(counter.get() / BLOCKS_PER_TICK + 1);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run(){
				if(historyTracker != null) {
					historyTracker.finalizeHistoricChanges();
				}
			}
		}, counter.get() * TICK_DELAY);

	}

	public boolean hasChangeAt(final BlockVector location, BlockFace direction){
		location.add(direction.getDirection());
		boolean result = changes.containsKey(location);
		location.subtract(direction.getDirection());
		return result;
	}

	public Collection<WorldChange> getChanges(){
		return changes.values();
	}
}
