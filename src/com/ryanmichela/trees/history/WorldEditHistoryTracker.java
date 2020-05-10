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
package com.ryanmichela.trees.history;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

public class WorldEditHistoryTracker{

	private final EditSession activeEditSession;
	private final Player forPlayer;
	private final NoChangeBukkitWorld localWorld;
	private final boolean enableUndo;

	public WorldEditHistoryTracker(final Location refPoint, final Player forPlayer, final boolean enableUndo){
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if(plugin == null) {
			throw new IllegalStateException("WorldEdit not loaded. Cannot create WorldEditHistoryTracker");
		}

		this.localWorld = new NoChangeBukkitWorld(refPoint.getWorld());
		final EditSession es = WorldEdit.getInstance().getEditSessionFactory()
				.getEditSession(new NoChangeBukkitWorld(refPoint.getWorld()), -1);
		this.activeEditSession = es;
		this.forPlayer = forPlayer;
		this.enableUndo = enableUndo;
	}

	public void finalizeHistoricChanges(){
		if (enableUndo) {
			final BukkitPlayer localPlayer = BukkitAdapter.adapt(forPlayer);
			final LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(localPlayer);
			activeEditSession.flushSession();
			localSession.remember(activeEditSession);
			localWorld.enableUndo();
		} else {
			activeEditSession.flushSession();
		}
	}

	public void recordHistoricChange(final Location changeLoc, final BlockData blockData){
		try{
			final BlockVector3 weVector = BukkitAdapter.asBlockVector(changeLoc);
			final BlockState weState = BukkitAdapter.adapt(blockData);
			activeEditSession.setBlock(weVector, weState.toBaseBlock());
		}
		catch(final MaxChangedBlocksException e){
			Bukkit.getLogger().severe("MaxChangedBlocksException!");
		}
	}
}
