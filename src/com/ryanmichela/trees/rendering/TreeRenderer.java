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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.arbaro.export.ExportError;
import net.arbaro.params.AbstractParam;
import net.arbaro.params.ParamError;
import net.arbaro.tree.Segment;
import net.arbaro.tree.Stem;
import net.arbaro.tree.Tree;

public class TreeRenderer{
	private final Plugin plugin;

	public TreeRenderer(final Plugin plugin){
		this.plugin = plugin;
	}

	public void renderTree(final Location refPoint, final File treeFile, final File rootFile, final int seed,
			final boolean withDelay){
		renderTree(refPoint, treeFile, rootFile, seed, false, null, withDelay);
	}

	public void renderTreeWithHistory(final Location refPoint, final File treeFile, final File rootFile, final int seed,
			final Player forPlayer, final boolean withDelay){
		renderTree(refPoint, treeFile, rootFile, seed, true, forPlayer, withDelay);
	}

	private Tree loadTree(final File treeFile, final int seed) throws IOException, ParamError{
		if(treeFile == null) return null;

		final Tree tree = new Tree();
		tree.setOutputType(Tree.CONES);
		tree.readFromXML(new FileInputStream(treeFile));
		tree.params.Seed = seed;
		tree.params.stopLevel = -1; // -1 for everything
		tree.params.verbose = false;
		return tree;
	}

	private void renderTree(final Location refPoint, final File treeFile, final File rootFile, final int seed,
			final boolean recordHistory, final Player forPlayer, final boolean withDelay){
		AbstractParam.loading = true;
		final WorldChangeTracker changeTracker = new WorldChangeTracker(plugin, recordHistory);

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
			@Override public void run(){
				try{
					plugin.getLogger().fine("Rendering tree " + treeFile.getName());
					final Tree tree = loadTree(treeFile, seed);
					tree.make();
					final TreeType treeType = new TreeType(tree.params.WoodType);
					final Draw3d d3d = new Draw3d(refPoint, tree.params.Smooth, treeType, changeTracker,
							Draw3d.RenderOrientation.NORMAL);

					final MinecraftExporter treeExporter = new MinecraftExporter(tree, d3d);
					treeExporter.write();

					if(tree.params.WoodType.equals("Jungle")) {
						JungleVinePopulator.populate(changeTracker, new Random(seed));
					}

					d3d.drawRootJunction(
							d3d.toMcVector(
									((Segment)((Stem)tree.trunks.get(0)).stemSegments().nextElement()).posFrom()),
							((Stem)tree.trunks.get(0)).baseRadius);

					if(rootFile != null && rootFile.exists()) {
						plugin.getLogger().fine("Rendering root " + rootFile.getName());
						final Tree root = loadTree(rootFile, seed);
						// Turn off leaves for roots and scale the roots the same as the tree
						root.params.Leaves = -1;
						root.params.scale_tree = tree.params.scale_tree;
						root.make();
						final TreeType rootType = new TreeType(root.params.WoodType);
						final Draw3d d3dInverted = new Draw3d(refPoint, root.params.Smooth, rootType, changeTracker,
								Draw3d.RenderOrientation.INVERTED);

						final MinecraftExporter treeExporterInverted = new MinecraftExporter(root, d3dInverted);
						treeExporterInverted.write();
					}
					AbstractParam.loading = false;

					final long generationDelay = withDelay ? plugin.getConfig().getInt("generation-delay", 0) * 20 : 0;

					plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

						@Override public void run(){
							try{
								d3d.applyChanges(forPlayer);
							}
							catch(final RuntimeException e){
								plugin.getLogger().severe("Error rendering tree: " + e.getMessage());
								e.printStackTrace();
								plugin.getLogger().severe(e.toString());
							}
						}
					}, generationDelay);

				}
				catch(final IOException | ParamError | ExportError e){
					plugin.getLogger().severe("Error rendering tree: " + e.getMessage());
				}
			}
		});
	}
}
