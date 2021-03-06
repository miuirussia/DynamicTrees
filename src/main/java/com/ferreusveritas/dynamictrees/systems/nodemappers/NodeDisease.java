package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
* Destroys all thin(radius == 1) branches on a tree.. leaving it to rot.
* @author ferreusveritas
*/
public class NodeDisease implements INodeInspector {

	Species species;//Destroy any thin branches made of the same kind of wood.

	public NodeDisease(Species tree) {
		this.species = tree;
	}

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null && species.getTree() == branch.getTree()) {
			if(branch.getRadius(world, pos) == 1) {
				world.setBlockToAir(pos);//Destroy the thin branch
			}
		}

		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

}
