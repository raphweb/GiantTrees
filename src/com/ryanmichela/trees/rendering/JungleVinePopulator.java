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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

public class JungleVinePopulator{

	public static void populate(final WorldChangeTracker tracker, final Random r){
		final BlockVector location = new BlockVector();

		final List<WorldChange> newChanges = new LinkedList<>();

		for(final WorldChange change : tracker.getChanges()){
			if((change.blockData.getMaterial() == Material.OAK_WOOD) || (change.blockData.getMaterial() == Material.JUNGLE_WOOD)) {
				location.setX(change.location.getBlockX());
				location.setY(change.location.getBlockY());
				location.setZ(change.location.getBlockZ());
				Stream.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST).forEach(direction -> {
					if ((r.nextInt(3) > 0) && (!tracker.hasChangeAt(location, direction))) {
						newChanges.add(new WorldChange(location.clone().add(direction.getDirection()),
								Bukkit.createBlockData(Material.VINE, "[" + direction.getOppositeFace().name().toLowerCase() + "=true]")));
					}
				});
			}
		}
		newChanges.forEach(change -> tracker.addChange(change, false));
	}
}
