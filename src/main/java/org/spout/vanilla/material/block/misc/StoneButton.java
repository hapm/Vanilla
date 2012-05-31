/*
 * This file is part of Vanilla.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * Vanilla is licensed under the SpoutDev License Version 1.
 *
 * Vanilla is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Vanilla is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.vanilla.material.block.misc;

import java.util.ArrayList;

import org.spout.api.entity.Entity;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.block.BlockFace;
import org.spout.api.material.block.BlockFaces;
import org.spout.api.util.LogicUtil;

import org.spout.vanilla.material.block.ScheduleUpdated;
import org.spout.vanilla.material.block.attachable.AbstractAttachable;
import org.spout.vanilla.material.block.attachable.PointAttachable;
import org.spout.vanilla.material.block.redstone.RedstoneSource;
import org.spout.vanilla.protocol.VanillaNetworkSynchronizer;
import org.spout.vanilla.protocol.msg.PlayEffectMessage;
import org.spout.vanilla.runnable.BlockScheduler;
import org.spout.vanilla.util.RedstonePowerMode;
import org.spout.vanilla.util.VanillaPlayerUtil;

import static org.spout.vanilla.util.VanillaNetworkUtil.playBlockEffect;

public class StoneButton extends AbstractAttachable implements PointAttachable, ScheduleUpdated, RedstoneSource {
	public StoneButton(String name, int id) {
		super(name, id);
		this.setAttachable(BlockFaces.NESW).setHardness(0.5F).setResistance(0.8F).setOpacity((byte) 1);
	}

	@Override
	public void onDelayedUpdate(Block block) {
		this.setPressed(block, false);
		playBlockEffect(block, null, PlayEffectMessage.Messages.RANDOM_CLICK_2);
		this.doRedstoneUpdates(block);
	}

	@Override
	public void doRedstoneUpdates(Block block) {
		block.setSource(this).update().translate(this.getAttachedFace(block)).update();
	}

	@Override
	public short getRedstonePower(Block block, RedstonePowerMode powerMode) {
		return this.hasRedstonePower(block, powerMode) ? REDSTONE_POWER_MAX : REDSTONE_POWER_MIN;
	}

	@Override
	public boolean hasRedstonePower(Block block, RedstonePowerMode powerMode) {
		return this.isPressed(block);
	}

	@Override
	public short getRedstonePowerTo(Block block, BlockFace direction, RedstonePowerMode powerMode) {
		return this.hasRedstonePower(block, powerMode) ? REDSTONE_POWER_MAX : REDSTONE_POWER_MIN;
	}

	@Override
	public boolean hasRedstonePowerTo(Block block, BlockFace direction, RedstonePowerMode powerMode) {
		return this.getAttachedFace(block) == direction && this.isPressed(block);
	}

	@Override
	public boolean canAttachTo(Block block, BlockFace face) {
		return face != BlockFace.TOP && super.canAttachTo(block, face);
	}

	@Override
	public void onInteractBy(Entity entity, Block block, Action type, BlockFace clickedFace) {
		super.onInteractBy(entity, block, type, clickedFace);
		if (type != Action.LEFT_CLICK || !VanillaPlayerUtil.isCreative(entity)) {
			if (!this.isPressed(block)) {
				this.setPressed(block, true);
				this.doRedstoneUpdates(block);
				BlockScheduler.schedule(block, 20);
				playBlockEffect(block, entity, PlayEffectMessage.Messages.RANDOM_CLICK_1);
			}
		}
	}

	@Override
	public void setAttachedFace(Block block, BlockFace attachedFace) {
		block.setData((short) (BlockFaces.NSEW.indexOf(attachedFace, 3) + 1));
	}

	@Override
	public BlockFace getAttachedFace(Block block) {
		return BlockFaces.NSEW.get((block.getData() & ~0x8) - 1);
	}

	public boolean isPressed(Block block) {
		return LogicUtil.getBit(block.getData(), 0x8);
	}

	public void setPressed(Block block, boolean pressed) {
		block.setData(LogicUtil.setBit(block.getData(), 0x8, pressed));
	}

	@Override
	public boolean isPlacementSuppressed() {
		return true;
	}

	@Override
	public ArrayList<ItemStack> getDrops(Block block) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(this, 1));
		return drops;
	}
}
