/*
 * This file is part of Vanilla (http://www.spout.org/).
 *
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
package org.spout.vanilla.controller;

import gnu.trove.map.hash.TIntObjectHashMap;

import org.spout.api.entity.ControllerRegistry;
import org.spout.vanilla.controller.living.MobControllerType;
import org.spout.vanilla.controller.living.creature.hostile.Blaze;
import org.spout.vanilla.controller.living.creature.hostile.CaveSpider;
import org.spout.vanilla.controller.living.creature.hostile.Creeper;
import org.spout.vanilla.controller.living.creature.hostile.Enderdragon;
import org.spout.vanilla.controller.living.creature.hostile.Ghast;
import org.spout.vanilla.controller.living.creature.hostile.Giant;
import org.spout.vanilla.controller.living.creature.hostile.MagmaCube;
import org.spout.vanilla.controller.living.creature.hostile.Silverfish;
import org.spout.vanilla.controller.living.creature.hostile.Skeleton;
import org.spout.vanilla.controller.living.creature.hostile.Slime;
import org.spout.vanilla.controller.living.creature.hostile.Spider;
import org.spout.vanilla.controller.living.creature.hostile.Zombie;
import org.spout.vanilla.controller.living.creature.neutral.Enderman;
import org.spout.vanilla.controller.living.creature.neutral.PigZombie;
import org.spout.vanilla.controller.living.creature.neutral.Wolf;
import org.spout.vanilla.controller.living.creature.passive.Chicken;
import org.spout.vanilla.controller.living.creature.passive.Cow;
import org.spout.vanilla.controller.living.creature.passive.Mooshroom;
import org.spout.vanilla.controller.living.creature.passive.Ocelot;
import org.spout.vanilla.controller.living.creature.passive.Pig;
import org.spout.vanilla.controller.living.creature.passive.Sheep;
import org.spout.vanilla.controller.living.creature.passive.Squid;
import org.spout.vanilla.controller.living.creature.passive.Villager;
import org.spout.vanilla.controller.living.creature.util.IronGolem;
import org.spout.vanilla.controller.living.creature.util.SnowGolem;
import org.spout.vanilla.controller.living.player.VanillaPlayer;
import org.spout.vanilla.controller.object.MovingBlock;
import org.spout.vanilla.controller.object.misc.EnderCrystal;
import org.spout.vanilla.controller.object.moving.Item;
import org.spout.vanilla.controller.object.moving.PrimedTnt;
import org.spout.vanilla.controller.object.moving.XPOrb;
import org.spout.vanilla.controller.object.projectile.Arrow;
import org.spout.vanilla.controller.object.projectile.BlazeFireball;
import org.spout.vanilla.controller.object.projectile.Egg;
import org.spout.vanilla.controller.object.projectile.EnderPearl;
import org.spout.vanilla.controller.object.projectile.EyeOfEnder;
import org.spout.vanilla.controller.object.projectile.GhastFireball;
import org.spout.vanilla.controller.object.projectile.Snowball;
import org.spout.vanilla.controller.object.vehicle.Boat;
import org.spout.vanilla.controller.object.vehicle.Minecart;
import org.spout.vanilla.protocol.entity.living.EndermanEntityProtocol;
import org.spout.vanilla.protocol.entity.living.GhastEntityProtocol;
import org.spout.vanilla.protocol.entity.living.SheepEntityProtocol;
import org.spout.vanilla.protocol.entity.living.VanillaPlayerEntityProtocol;
import org.spout.vanilla.protocol.entity.object.FallingBlockProtocol;
import org.spout.vanilla.protocol.entity.object.PickupEntityProtocol;
import org.spout.vanilla.protocol.entity.object.XPOrbEntityProtocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Enum that serves as a lookup for all controllers in Vanilla.
 */
public class VanillaControllerTypes {
	public static final VanillaControllerType DROPPED_ITEM  = new VanillaControllerType(1, Item.class, "Item", new PickupEntityProtocol());
	public static final VanillaControllerType XP_ORB = new VanillaControllerType(2, XPOrb.class, "XP Orb", new XPOrbEntityProtocol());
	public static final VanillaControllerType SHOT_ARROW = new VanillaControllerType(10, Arrow.class, "Arrow");
	public static final VanillaControllerType THROWN_SNOWBALL = new VanillaControllerType(11, Snowball.class, "Snowball");
	public static final VanillaControllerType GHAST_FIREBALL = new VanillaControllerType(12, GhastFireball.class, "Fireball");
	public static final VanillaControllerType BLAZE_FIREBALL = new VanillaControllerType(13, BlazeFireball.class, "Blaze Fireball");
	public static final VanillaControllerType THROWN_ENDER_PEARL = new VanillaControllerType(14, EnderPearl.class, "Ender Pearl");
	public static final VanillaControllerType THROWN_EYE_OF_ENDER = new VanillaControllerType(15, EyeOfEnder.class, "Eye of Ender");
	public static final VanillaControllerType EGG = new VanillaControllerType(15, Egg.class, "Egg");
	public static final VanillaControllerType PRIMED_TNT = new VanillaControllerType(16, PrimedTnt.class, "Primed TNT");
	public static final VanillaControllerType FALLING_BLOCK = new VanillaControllerType(21, MovingBlock.class, "Falling Block", new FallingBlockProtocol());
	public static final VanillaControllerType MINECART = new VanillaControllerType(40, Minecart.class, "Minecart");
	public static final VanillaControllerType BOAT = new VanillaControllerType(41, Boat.class, "Boat");
	public static final VanillaControllerType CREEPER = new MobControllerType(50, Creeper.class, "Creeper");
	public static final VanillaControllerType SKELETON = new MobControllerType(51, Skeleton.class, "Skeleton");
	public static final VanillaControllerType SPIDER = new MobControllerType(52, Spider.class, "Spider");
	public static final VanillaControllerType GIANT = new MobControllerType(53, Giant.class, "Giant");
	public static final VanillaControllerType ZOMBIE = new MobControllerType(54, Zombie.class, "Zombie");
	public static final VanillaControllerType SLIME = new MobControllerType(55, Slime.class, "Slime");
	public static final VanillaControllerType GHAST = new MobControllerType(56, Ghast.class, "Ghast", new GhastEntityProtocol());
	public static final VanillaControllerType PIG_ZOMBIE = new MobControllerType(57, PigZombie.class, "Zombie Pigman");
	public static final VanillaControllerType ENDERMAN = new MobControllerType(58, Enderman.class, "Enderman", new EndermanEntityProtocol());
	public static final VanillaControllerType CAVE_SPIDER = new MobControllerType(59, CaveSpider.class, "Cave Spider");
	public static final VanillaControllerType SILVERFISH = new MobControllerType(60, Silverfish.class, "Silverfish");
	public static final VanillaControllerType BLAZE = new MobControllerType(61, Blaze.class, "Blaze");
	public static final VanillaControllerType MAGMA_CUBE = new MobControllerType(62, MagmaCube.class, "Magma Cube");
	public static final VanillaControllerType ENDEDRAGON = new MobControllerType(64, Enderdragon.class, "Enderdragon");
	public static final VanillaControllerType PIG = new MobControllerType(90, Pig.class, "Pig");
	public static final VanillaControllerType SHEEP = new MobControllerType(91, Sheep.class, "Sheep", new SheepEntityProtocol());
	public static final VanillaControllerType COW = new MobControllerType(92, Cow.class, "Cow");
	public static final VanillaControllerType CHICKEN = new MobControllerType(93, Chicken.class, "Chicken");
	public static final VanillaControllerType SQUID = new MobControllerType(94, Squid.class, "Squid");
	public static final VanillaControllerType WOLF = new MobControllerType(95, Wolf.class, "Wolf");
	public static final VanillaControllerType MOOSHROOM = new MobControllerType(96, Mooshroom.class, "Mooshroom");
	public static final VanillaControllerType SNOW_GOLEM = new MobControllerType(97, SnowGolem.class, "Snow Golem");
	public static final VanillaControllerType OCELOT = new MobControllerType(98, Ocelot.class, "Ocelot");
	public static final VanillaControllerType VILLAGER = new MobControllerType(120, Villager.class, "Villager");
	public static final VanillaControllerType ENDER_CRYSTAL = new VanillaControllerType(200, EnderCrystal.class, "Ender Crystal");
	public static final VanillaControllerType IRON_GOLEM = new MobControllerType(99, IronGolem.class, "Iron Golem");
	public static final VanillaControllerType PLAYER = new VanillaControllerType(-1, VanillaPlayer.class, "Player", new VanillaPlayerEntityProtocol());

	public static final String KEY = "ControllerID";
	private static final TIntObjectHashMap<VanillaControllerType> map = new TIntObjectHashMap<VanillaControllerType>();

	public static VanillaControllerType getByID(int id) {
		return map.get(id);
	}

	static {
		for (Field field : VanillaControllerTypes.class.getFields())  {
			if (Modifier.isStatic(field.getModifiers()) && VanillaControllerType.class.isAssignableFrom(field.getType())) {
				try {
					VanillaControllerType type = (VanillaControllerType) field.get(null);
					ControllerRegistry.register(type);
					map.put(type.getSpawnId(), type);
				} catch (IllegalAccessException e) {
					continue;
				}
			}
		}
	}
}