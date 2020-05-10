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
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

public class WorldEditHistoryTracker{

	private final EditSession activeEditSession;
	private final Player forPlayer;
	private final NoChangeBukkitWorld localWorld;
	private final WorldEditPlugin wePlugin;
	private final boolean enableUndo;

	public WorldEditHistoryTracker(final Location refPoint, final Player forPlayer, final boolean enableUndo){
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if(plugin == null) {
			throw new IllegalStateException("WorldEdit not loaded. Cannot create WorldEditHistoryTracker");
		}
		wePlugin = (WorldEditPlugin)plugin;

		localWorld = new NoChangeBukkitWorld(refPoint.getWorld());
		// No public alternative
		//EditSessionFactory esf = new EditSessionFactory();
		//final EditSession es = esf.getEditSession(localWorld, Integer.MAX_VALUE);
		final EditSession es = WorldEdit.getInstance().getEditSessionFactory()
				.getEditSession(new NoChangeBukkitWorld(refPoint.getWorld()), -1);
		activeEditSession = es;
		//activeEditSession.enableStandardMode();
		//activeEditSession.setMask((com.sk89q.worldedit.function.mask.Mask)null);
		//activeEditSession.setFastMode(true);
		this.forPlayer = forPlayer;
		this.enableUndo = enableUndo;
	}

	public void finalizeHistoricChanges(){
		final BukkitPlayer localPlayer = new BukkitPlayer(wePlugin, forPlayer);
		final LocalSession localSession = wePlugin.getWorldEdit().getSessionManager().get(localPlayer);
		activeEditSession.flushSession();
		localSession.remember(activeEditSession);
		if (enableUndo) {
			localWorld.enableUndo();
		}
	}

	public int getBlockChangeCount(){
		return activeEditSession.getBlockChangeCount();
	}

	public int getSize(){
		return activeEditSession.size();
	}

	public void recordHistoricChange(final Location changeLoc, final BlockData blockData){
		try{
			final BlockVector3 weVector = BlockVector3.at(
					changeLoc.getBlockX(), changeLoc.getBlockY(), changeLoc.getBlockZ());
			final BlockType weType = new BlockType(blockData.getMaterial().name().toLowerCase());
			final BlockState weState = weType.getDefaultState();
			//Property<?> axisProp = weType.getPropertyMap().getOrDefault("axis", null);
			//if (axisProp != null) {
			//	EnumProperty axisEnumProp = (EnumProperty)axisProp;
			//	String currentAxisValue = weState.getState(axisEnumProp);
			//	if (!currentAxisValue.equals("y"))
			//		weState = weState.with(axisEnumProp, "y");
			//}
			activeEditSession.setBlock(weVector, weState.toBaseBlock());
		}
		catch(final MaxChangedBlocksException e){
			Bukkit.getLogger().severe("MaxChangedBlocksException!");
		}
	}
}
