package com.ferreusveritas.dynamictrees.items;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class Seed extends Item {

	public final static Seed NULLSEED = new Seed("null") {
		{ setCreativeTab(null); }
		@Override public void setSpecies(Species species, ItemStack seedStack) {}
		@Override public Species getSpecies(ItemStack seedStack) { return Species.NULLSPECIES; }
		@Override public boolean onEntityItemUpdate(EntityItem entityItem) { entityItem.setDead(); return false; }
		@Override public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) { return EnumActionResult.FAIL; }
	};
	
	private Species species;//The tree this seed creates

	public Seed(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	public void setSpecies(Species species, ItemStack seedStack) {
		this.species = species;
	}
	
	public Species getSpecies(ItemStack seedStack) {
		return species;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		World world = CompatHelper.getEntityWorld(entityItem);
		
		if(entityItem.ticksExisted >= ModConfigs.seedTimeToLive) {//1 minute by default(helps with lag)
			if(!world.isRemote) {//Server side only
				BlockPos pos = new BlockPos(entityItem);
				if(world.canBlockSeeSky(pos)) {
					Random rand = new Random();
					ItemStack seedStack = CompatHelper.getEntityItem(entityItem);
					int count = CompatHelper.getStackCount(seedStack);
					while(count-- > 0) {
						if( getSpecies(seedStack).biomeSuitability(world, pos) * ModConfigs.seedPlantRate > rand.nextFloat()){
							if(getSpecies(seedStack).plantSapling(world, pos)) {
								break;
							}
						}
					}
					CompatHelper.setStackCount(CompatHelper.getEntityItem(entityItem), 0);
				}
			}
			entityItem.setDead();
		}

		return false;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack seedStack = player.getHeldItem(hand);

		//Handle Flower Pot interaction
		IBlockState blockState = world.getBlockState(pos);
		if(blockState.equals(Blocks.FLOWER_POT.getDefaultState())) { //Empty Flower Pot
			Species species = getSpecies(seedStack);
			BlockBonsaiPot bonzaiPot = species.getTree().getBonzaiPot();//FIXME: Species need their own bonsai pots.. or find another solution
			if(bonzaiPot.setSpecies(world, species, pos)) {
				CompatHelper.shrinkStack(seedStack, 1);
				return EnumActionResult.SUCCESS;
			}
		}
		
		if (facing == EnumFacing.UP) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(pos, facing, seedStack) && player.canPlayerEdit(pos.up(), facing, seedStack)) {//Ensure permissions to edit block
				if(getSpecies(seedStack).plantSapling(world, pos.up())) {//Do the planting
					CompatHelper.shrinkStack(seedStack, 1);
					return EnumActionResult.SUCCESS;
				}
			}
		}

		return EnumActionResult.FAIL;
	}
	
}
