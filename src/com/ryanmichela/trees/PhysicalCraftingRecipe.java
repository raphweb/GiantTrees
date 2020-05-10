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
package com.ryanmichela.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * PhysicalCraftingRecipe matches blocks laid in a pattern horizontally in the
 * world.
 */
public class PhysicalCraftingRecipe{

	private final List<List<Set<BlockData>>> pattern;
	private final Set<Material> usedMaterials;

	/**
	 * Creates a PhysicalCraftingRecipe from a 2D pattern of Materials and data
	 * values. The pattern must
	 * be rectangular - all rows must be the same length. Data must be the same
	 * dimensions as pattern.
	 * A data value of -1 matches all data values.
	 *
	 * @param pattern
	 *            The rectangle of Materials to match.
	 */
	public PhysicalCraftingRecipe(final List<List<Set<BlockData>>> pattern){
		Validate.notEmpty(pattern, "pattern cannot be null or empty");
		this.pattern = pattern;
		this.usedMaterials = pattern.stream().flatMap(List::stream).flatMap(Set::stream).map(BlockData::getMaterial).collect(Collectors.toSet());
	}

	/**
	 * Constructs a PhysicalCraftingRecipe from an array of strings. Each character in the array represents one or more
	 * combination of material/blockdata encoded in char2MaterialDataStr.
	 *
	 * @param rows
	 *            The characters making up the crafting recipe
	 * @param char2MaterialDataStr
	 *            The materials with block data as string to match in the crafting recipe
	 * @return
	 */
	public static PhysicalCraftingRecipe fromStringRepresentation(final List<String> rows, final Map<Character, String> char2MaterialDataStr){
		// Sanity check the input
		Validate.notEmpty(rows, "rows cannot be null or empty");
		Validate.notEmpty(char2MaterialDataStr, "materialMap cannot be null or empty");
		
		// transform string representation of material and block data to sets of BlockData
		final Map<Character, Set<BlockData>> char2MaterialData = char2MaterialDataStr.entrySet().stream()
				// remap string values to sets of block data
				.collect(Collectors.toMap(k -> k.getKey(), v ->
					Stream.of(v.getValue().split(",")).map(Bukkit::createBlockData).collect(Collectors.toSet())));

		// Validate the relationship between rows and maps
		final int rowLength = rows.get(0).length();
		for(final String row : rows){
			if(row.length() != rowLength) {
				throw new IllegalArgumentException("all strings in rows must be the same length");
			}
			for(final char c : row.toCharArray()){
				if(!char2MaterialData.containsKey(c)) {
					throw new IllegalArgumentException("all characters in rows must be in materialMap");
				}
			}
		}

		// Construct the pattern
		final List<List<Set<BlockData>>> pattern = new ArrayList<>();
		for(int row = 0; row < rows.size(); row++){
			final List<Set<BlockData>> rowArray = new ArrayList<>();
			pattern.add(rowArray);
			final char colsInRow[] = rows.get(row).toCharArray();
			for(int col = 0; col < rowLength; col++){
				final char c = colsInRow[col];
				rowArray.add(char2MaterialData.get(c));
			}
		}

		return new PhysicalCraftingRecipe(pattern);
	}

	/**
	 * Determines if this PhysicalCraftingRecipe matches the blocks in the
	 * world.
	 *
	 * @param lastPlaced
	 *            A starting point for evaluating this recipe.
	 * @return
	 */
	public boolean matches(final Block lastPlaced){
		Validate.notNull(lastPlaced, "lastPlaced cannot be null");

		// Verify that the block placed could be part of the pattern
		if(!usedMaterials.contains(lastPlaced.getType())) {
			return false;
		}
		final int y = lastPlaced.getY();
		// Scan the world looking for a match
		final int size = Math.max(pattern.size(), pattern.get(0).size());
		int patternMatchCount = 0;
		for(int x = (lastPlaced.getX() - size) + 1; x <= lastPlaced.getX(); x++){
			for(int z = (lastPlaced.getZ() - size) + 1; z <= lastPlaced.getZ(); z++){
				if (allRowPass(false, false, lastPlaced, x, y, z) ||
					allRowPass(true,  false, lastPlaced, x, y, z) ||
					allRowPass(false, true,  lastPlaced, x, y, z) ||
					allRowPass(true,  true,  lastPlaced, x, y, z)) {
					patternMatchCount++;
				}
			}
		}
		return patternMatchCount == 1;
	}
	
	private boolean allRowPass(final boolean xInverted, final boolean zInverted, final Block lastPlaced, final int x, final int y, final int z) {
		final IntStream pxStream = IntStream.range(0, pattern.size()       ).map(i -> xInverted ? pattern.size()        - i - 1 : i);
		final IntStream pzStream = IntStream.range(0, pattern.get(0).size()).map(i -> zInverted ? pattern.get(0).size() - i - 1 : i);
		return pxStream.allMatch(px -> pzStream.allMatch(pz -> {
			final Block b = lastPlaced.getWorld().getBlockAt(x + px, y, z + pz);
			// current block data considered at pattern position px, pz
			final Set<BlockData> cp = pattern.get(px).get(pz);
			return cp.isEmpty() || cp.stream().anyMatch(d -> b.getBlockData().matches(d) && b.getType() == d.getMaterial());
		}));
	}
}
