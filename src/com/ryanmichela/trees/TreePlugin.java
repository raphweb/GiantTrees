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
import java.util.stream.Stream;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TreePlugin extends JavaPlugin{

	private class WorldInitListener implements Listener{
		@EventHandler public void onWorldInit(final WorldInitEvent evt){
			for(final String worldName : getConfig().getStringList("worlds")){
				if(worldName.equals(evt.getWorld().getName())) {
					getLogger().info("Attaching giant tree populator to world \""+evt.getWorld().getName() + "\"");
					evt.getWorld().getPopulators().add(new TreePopulator(TreePlugin.this));
					return;
				}
			}
		}
	}

	@Override public void onEnable(){
		try{
			getServer().getPluginManager().registerEvents(new PlantTreeEventHandler(this), this);
			// attach to worlds automatically when onlyUseWorldManagers is false
			if(getConfig().getBoolean("naturallyGrowTrees", true)) {
				getServer().getPluginManager().registerEvents(new WorldInitListener(), this);
			}
		}
		catch(final Exception e){
			getLogger().severe("Failed to initialize plugin: " + e.getMessage());
		}

		if(getServer().getPluginManager().getPlugin("WorldEdit") == null) {
			getLogger().warning("WorldEdit not installed. Undo capability disabled.");
		}

		getCommand("tree-edit").setExecutor(new EditTreeCommand(this));
		getCommand("tree-create").setExecutor(new CreateTreeCommand(this));
	}

	@Override public void onLoad(){
		if(!getDataFolder().exists()) saveDefaultConfig();

		// unpack basic trees
		Stream.of("BIRCH_FOREST", "FOREST", "JUNGLE", "ROOFED_FOREST", "SAVANNA", "TAIGA").forEach(biome -> ensureTreeFileExists("biome." + biome));
		Stream.of("ACACIA", "BIRCH", "DARK_OAK", "JUNGLE", "OAK", "SPRUCE").forEach(tree -> ensureTreeFileExists("tree." + tree));
	}

	private void ensureTreeFileExists(String filePrefix){
		String treeFile = filePrefix + ".xml";
		String treeRootFile = filePrefix + ".root.xml";
		// Check before creation, to silence meaningless warnings
		if(!new File(getDataFolder(), treeFile).exists()) saveResource("resources/"+treeFile, true);
		if(!new File(getDataFolder(), treeRootFile).exists()) saveResource("resources/"+treeRootFile, true);
	}
}