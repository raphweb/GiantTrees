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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class TreeType{

	public final BlockData leafMaterial;
	public final BlockData woodMaterial;

	public TreeType(final String treeType){
		// "Oak","Spruce","Birch","Jungle","Acacia","Dark Oak"
		woodMaterial = Bukkit.createBlockData(Material.matchMaterial(treeType + " Wood"), "[axis=y]");
		leafMaterial = Bukkit.createBlockData(Material.matchMaterial(treeType + " Leaves"), "[persistent=true]");
	}
}
