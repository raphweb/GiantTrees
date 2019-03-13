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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.ryanmichela.trees.rendering.TreeRenderer;

public class PlantTreeEventHandler implements Listener{

	private int boneMealConsumed;
	private boolean enabled;
	private final Plugin plugin;
	private PhysicalCraftingRecipe recipe;
	private final TreeRenderer renderer;

	public PlantTreeEventHandler(final Plugin pl){
		plugin = pl;
		renderer = new TreeRenderer(plugin);

		try{
			// Read the raw config
			final ConfigurationSection patternSection = plugin.getConfig().getConfigurationSection("planting-pattern");
			final List<String> rows = patternSection.getStringList("pattern");
			final ConfigurationSection materialsSection = patternSection.getConfigurationSection("materials");
			final Map<String, Object> configMaterialMap = materialsSection.getValues(false);

			final Map<Character, String> materialDataMap = new HashMap<>();
			for(final Map.Entry<String, Object> kvp : configMaterialMap.entrySet()){
				materialDataMap.put(kvp.getKey().charAt(0), (String)kvp.getValue());
			}

			boneMealConsumed = patternSection.getInt("bone-meal-consumed");

			recipe = PhysicalCraftingRecipe.fromStringRepresentation(rows.toArray(new String[]{}), materialDataMap);
			if(!recipe.usedMaterials.contains(Material.OAK_SAPLING)) {
				throw new Exception();
			}

			enabled = true;
		}
		catch(final Exception e){
			plugin.getLogger().severe("The planting-pattern config section is invalid! "
					+ "Disabling survival planting of giant trees.");
			enabled = false;
		}
	}

	@EventHandler public void onPlayerInteract(final PlayerInteractEvent evt){
		if(!enabled || !evt.getPlayer().hasPermission("gianttrees.grow")) return;

		ItemStack itemInHand = evt.getItem();
		final Block clickedBlock = evt.getClickedBlock();
		if(itemInHand == null || clickedBlock == null || evt.getAction() != Action.RIGHT_CLICK_BLOCK
				|| itemInHand.getType() != Material.BONE_MEAL || itemInHand.getAmount() < boneMealConsumed
				|| !recipe.matches(clickedBlock)) return;

		final String treeType = getTreeName(clickedBlock.getType());
		final Random seed = new Random(clickedBlock.getWorld().getSeed());
		final File treeFile = new File(plugin.getDataFolder(), "tree." + treeType + ".xml");
		final File rootFile = new File(plugin.getDataFolder(), "tree." + treeType + ".root.xml");
		evt.setCancelled(true);
		if(itemInHand.getAmount()-15 <= 0) itemInHand = new ItemStack(Material.AIR);
		else itemInHand.setAmount(itemInHand.getAmount()-boneMealConsumed);

		// Take bonemeal
		if(evt.getHand() == EquipmentSlot.HAND) evt.getPlayer().getInventory().setItemInMainHand(itemInHand);
		else evt.getPlayer().getInventory().setItemInOffHand(itemInHand);

		evt.getPlayer().sendMessage(ChatColor.GOLD+"Stand back!");
		renderer.renderTree(clickedBlock.getLocation(), treeFile, rootFile, seed.nextInt(), true);
	}

	private String getTreeName(final Material treeType){
		switch(treeType){
			case OAK_SAPLING:
				return "OAK";
			case SPRUCE_SAPLING:
				return "SPRUCE";
			case BIRCH_SAPLING:
				return "BIRCH";
			case JUNGLE_SAPLING:
				return "JUNGLE";
			case ACACIA_SAPLING:
				return "ACACIA";
			case DARK_OAK_SAPLING:
				return "DARK_OAK";
			default:
				return "OAK";
		}
	}
}
