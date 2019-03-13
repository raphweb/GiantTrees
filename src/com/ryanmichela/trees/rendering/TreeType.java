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

import org.bukkit.Material;

public class TreeType{

//	public byte dataOffset;
	public Material leafMaterial;
	public Material woodMaterial;

	public TreeType(final String treeType){
		// "Oak","Spruce","Birch","Jungle","Acacia","Dark Oak"
		if(treeType.equals("Oak")) {
			woodMaterial = Material.OAK_LOG;
			leafMaterial = Material.OAK_LEAVES;
//			dataOffset = 0;
		}
		else if(treeType.equals("Spruce")) {
			woodMaterial = Material.SPRUCE_LOG;
			leafMaterial = Material.SPRUCE_LEAVES;
//			dataOffset = 1;
		}
		else if(treeType.equals("Birch")) {
			woodMaterial = Material.BIRCH_LOG;
			leafMaterial = Material.BIRCH_LEAVES;
//			dataOffset = 2;
		}
		else if(treeType.equals("Jungle")) {
			woodMaterial = Material.JUNGLE_LOG;
			leafMaterial = Material.JUNGLE_LEAVES;
//			dataOffset = 3;
		}
		else if(treeType.equals("Acacia")) {
			woodMaterial = Material.ACACIA_LOG;
			leafMaterial = Material.ACACIA_LEAVES;
//			dataOffset = 0;
		}
		else if(treeType.equals("Dark Oak")) {
			woodMaterial = Material.DARK_OAK_LOG;
			leafMaterial = Material.DARK_OAK_LEAVES;
//			dataOffset = 1;
		}
		else{
			woodMaterial = Material.OAK_LOG;
			leafMaterial = Material.OAK_LEAVES;
//			dataOffset = 0;
		}
	}
}
