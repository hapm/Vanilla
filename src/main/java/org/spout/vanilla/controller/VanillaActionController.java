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
package org.spout.vanilla.controller;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.spout.api.Source;
import org.spout.api.Spout;
import org.spout.api.collision.BoundingBox;
import org.spout.api.collision.CollisionModel;
import org.spout.api.collision.CollisionStrategy;
import org.spout.api.entity.Entity;
import org.spout.api.entity.PlayerController;
import org.spout.api.entity.action.ActionController;
import org.spout.api.event.entity.EntityHealthChangeEvent;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.inventory.ItemStack;
import org.spout.api.math.MathHelper;
import org.spout.api.math.Quaternion;
import org.spout.api.math.Vector2;
import org.spout.api.math.Vector3;

import org.spout.vanilla.controller.object.moving.Item;
import org.spout.vanilla.controller.source.DamageCause;
import org.spout.vanilla.controller.source.HealthChangeReason;
import org.spout.vanilla.event.entity.EntityCombustEvent;
import org.spout.vanilla.protocol.msg.AnimationMessage;
import org.spout.vanilla.protocol.msg.EntityStatusMessage;
import org.spout.vanilla.util.VanillaNetworkUtil;

import static org.spout.vanilla.util.VanillaNetworkUtil.broadcastPacket;

/**
 * Controller that is the parent of all entity controllers.
 */
public abstract class VanillaActionController extends ActionController implements VanillaController {
	private final VanillaControllerType type;
	private final BoundingBox area = new BoundingBox(-0.3F, 0F, -0.3F, 0.3F, 0.8F, 0.3F);
	private static Random rand = new Random();
	// Controller flags
	private boolean isFlammable = true;
	// Tick effects
	private int fireTicks = 0;
	private int positionTicks = 0;
	private int velocityTicks = 0;
	protected int airTicks = 0;
	// Velocity-related
	private Vector3 velocity = Vector3.ZERO;
	private Vector3 movementSpeed = Vector3.ZERO;
	private Vector3 maxSpeed = Vector3.ZERO;
	// Controller characteristics
	private int health = 1;
	private int maxHealth = 1;
	// Damage
	private Source lastDamage = DamageCause.UNKNOWN;
	private VanillaActionController lastDamager;

	protected VanillaActionController(VanillaControllerType type) {
		super(type);
		this.type = type;
	}

	@Override
	public void onAttached() {
		getParent().setCollision(new CollisionModel(area));
		getParent().getCollision().setStrategy(CollisionStrategy.SOLID);
		data().put(VanillaControllerTypes.KEY, getType().getID());

		// Load data
		isFlammable = (Boolean) data().get("flammable", isFlammable);
		fireTicks = (Integer) data().get("fire_ticks", fireTicks);
		positionTicks = (Integer) data().get("position_ticks", positionTicks);
		velocityTicks = (Integer) data().get("velocity_ticks", velocityTicks);
		velocity = (Vector3) data().get("velocity", velocity);
		movementSpeed = (Vector3) data().get("movement_speed", movementSpeed);
		maxSpeed = (Vector3) data().get("max_speed", maxSpeed);
		health = (Integer) data().get("health", maxHealth);
		maxHealth = (Integer) data().get("max_health", maxHealth);
		airTicks = (Integer) data().get("air_ticks", airTicks);
	}

	@Override
	public void onSave() {
		data().put("flammable", isFlammable);
		data().put("fire_ticks", fireTicks);
		data().put("position_ticks", positionTicks);
		data().put("velocity_ticks", velocityTicks);
		data().put("velocity", velocity);
		data().put("movement_speed", movementSpeed);
		data().put("max_speed", maxSpeed);
		data().put("health", health);
		data().put("max_health", maxHealth);
		data().put("air_ticks", airTicks);
	}

	@Override
	public void onTick(float dt) {
		checkFireTicks();

		// Check controller health, send messages to the client based on current state.
		if (health <= 0) {
			VanillaNetworkUtil.broadcastPacket(new EntityStatusMessage(getParent().getId(), EntityStatusMessage.ENTITY_DEAD));
			if (!(this instanceof PlayerController)) {
				getParent().kill();
			}
			onDeath();
		}

		positionTicks++;
		velocityTicks++;

		super.onTick(dt);
	}

	@Override
	public void onCollide(Block block) {
		setVelocity(Vector3.ZERO);
	}

	@Override
	public void onCollide(Entity entity) {
		// push entities apart
		// TODO: Ignore if this entity is a passenger?
		Vector2 diff = entity.getPosition().subtract(this.getParent().getPosition()).toVector2();
		float distance = diff.length();
		if (distance > 0.1f) {
			double factor = Math.min(1f / distance, 1f) / distance * 0.05;
			diff = diff.multiply(factor);
			setVelocity(getVelocity().add(diff.toVector3()));
		}
	}

	@Override
	public void onDeath() {
		for (ItemStack drop : getDrops(lastDamage, lastDamager)) {
			if (drop == null) {
				continue;
			}
			Item item = new Item(drop, Vector3.ZERO);
			getParent().getLastTransform().getPosition().getWorld().createAndSpawnEntity(getParent().getLastTransform().getPosition(), item);
			// TODO: Drop experience
		}
	}

	@Override
	public VanillaControllerType getType() {
		return type;
	}

	public BoundingBox getBounds() {
		return this.area;
	}

	public boolean needsVelocityUpdate() {
		return velocityTicks % 5 == 0;
	}

	public boolean needsPositionUpdate() {
		return positionTicks % 60 == 0;
	}

	public Vector3 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector3 velocity) {
		if (velocity == null) {
			if (Spout.debugMode()) {
				Spout.getLogger().log(Level.SEVERE, "Velocity of " + this.toString() + " set to null!");
				Spout.getLogger().log(Level.SEVERE, "Report this to http://issues.spout.org");
			}
			velocity = Vector3.ZERO;
		}
		this.velocity = velocity;
	}

	/**
	 * Gets the speed of the controller during the prior movement. This will always be lower than the maximum speed.
	 * @return
	 */
	public Vector3 getMovementSpeed() {
		return movementSpeed;
	}

	/**
	 * Gets the maximum speed this controller is allowed to move at once.
	 * @return
	 */
	public Vector3 getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(Vector3 maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	/**
	 * Get the drops that Vanilla controllers disperse into the world when un-attached (such as entity death). Children controllers should override this method for their own personal drops.
	 * @param source Source of death
	 * @param lastDamager Controller that killed this controller, can be null if death was caused by natural sources such as drowning or burning
	 * @return the drops to disperse.
	 */
	public Set<ItemStack> getDrops(Source source, VanillaActionController lastDamager) {
		return new HashSet<ItemStack>();
	}

	/**
	 * Checks to see if the controller is combustible.
	 * @return true is combustible, false if not
	 */
	public boolean isFlammable() {
		return isFlammable;
	}

	/**
	 * Sets if the controller is combustible or not.
	 * @param isFlammable flag representing combustible status.
	 */
	public void setFlammable(boolean isFlammable) {
		this.isFlammable = isFlammable;
	}

	/**
	 * Gets the amount of ticks the controller has been on fire.
	 * @return amount of ticks
	 */
	public int getFireTicks() {
		return fireTicks;
	}

	/**
	 * Sets the amount of ticks the controller has been on fire.
	 * @param fireTicks the new amount of ticks the controller has been on fire for.
	 */
	public void setFireTicks(int fireTicks) {
		if (fireTicks > 0) {
			EntityCombustEvent event = Spout.getEventManager().callEvent(new EntityCombustEvent(getParent(), fireTicks));
			fireTicks = event.getDuration();
		}
		this.fireTicks = fireTicks;
	}

	private void checkFireTicks() {
		if (fireTicks > 0) {
			if (!isFlammable) {
				fireTicks -= 4;
				if (fireTicks < 0) {
					fireTicks = 0;
				}
				return;
			}

			if (fireTicks % 20 == 0) {
				damage(1, DamageCause.FIRE_CONTACT);
			}

			--fireTicks;
		}
	}

	// =========================
	// Controller helper methods
	// =========================

	/**
	 * Damages this controller with the given {@link DamageCause}.
	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
	 */
	public void damage(int amount) {
		damage(amount, DamageCause.UNKNOWN);
	}

	/**
	 * Damages this controller with the given {@link DamageCause}.
	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
	 * @param cause cause of this controller being damaged
	 */
	public void damage(int amount, DamageCause cause) {
		damage(amount, cause, true);
	}

	/**
	 * Damages this controller with the given {@link DamageCause}.
	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
	 * @param cause cause of this controller being damaged
	 * @param sendHurtMessage whether to send the hurt packet to all players online
	 */
	public void damage(int amount, DamageCause cause, boolean sendHurtMessage) {
		damage(amount, cause, null, sendHurtMessage);
	}

	/**
	 * Damages this controller with the given {@link DamageCause} and damager.
	 * @param amount amount the controller will be damaged by, can be modified based on armor and enchantments
	 * @param cause cause of this controller being damaged
	 * @param damager controller that damaged this controller
	 * @param sendHurtMessage whether to send the hurt packet to all players online
	 */
	public void damage(int amount, DamageCause cause, VanillaActionController damager, boolean sendHurtMessage) {
		// TODO take potion effects into account
		setHealth(getHealth() - amount, HealthChangeReason.DAMAGE);
		lastDamager = damager;
		if (sendHurtMessage) {
			broadcastPacket(new AnimationMessage(this.getParent().getId(), AnimationMessage.ANIMATION_HURT), new EntityStatusMessage(this.getParent().getId(), EntityStatusMessage.ENTITY_HURT));
		}
	}

	/**
	 * Moves this controller.
	 * @param vect the vector that is applied as the movement.
	 */
	public void move(Vector3 vect) {
		getParent().translate(vect);
	}

	/**
	 * Uses the current velocity and maximum speed to move this entity.
	 */
	public void move() {
		movementSpeed = MathHelper.min(velocity, maxSpeed);
		move(movementSpeed);
	}

	/**
	 * Moves this controller
	 * @param x x-axis to move the controller along
	 * @param y y-axis to move the controller along
	 * @param z z-axis to move the controller along
	 */
	public void move(float x, float y, float z) {
		move(new Vector3(x, y, z));
	}

	/**
	 * Rotates the controller
	 * @param rot the quaternion that is applied as the rotation.
	 */
	public void rotate(Quaternion rot) {
		getParent().rotate(rot);
	}

	/**
	 * Rotates the controller
	 * @param degrees the angle of which to do rotation.
	 * @param x x-axis to rotate the controller along
	 * @param y y-axis to rotate the controller along
	 * @param z z-axis to rotate the controller along
	 */
	public void rotate(float degrees, float x, float y, float z) {
		getParent().rotate(degrees, x, y, z);
	}

	public void yaw(float angle) {
		getParent().yaw(angle);
	}

	public void pitch(float angle) {
		getParent().pitch(angle);
	}

	/**
	 * Rolls this controller along an angle.
	 * @param angle the angle in-which to roll
	 */
	public void roll(float angle) {
		getParent().roll(angle);
	}

	public Vector3 getPreviousPosition() {
		return getParent().getLastTransform().getPosition();
	}

	public Quaternion getPreviousRotation() {
		return getParent().getLastTransform().getRotation();
	}

	/**
	 * If a child controller needs a random number for anything, they should call this method. This eliminates needless random objects created all the time.
	 * @return random object.
	 */
	public Random getRandom() {
		return rand;
	}

	public int getHealth() {
		return health;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setHealth(int health, Source source) {
		EntityHealthChangeEvent event = new EntityHealthChangeEvent(getParent(), source, health);
		Spout.getEngine().getEventManager().callEvent(event);
		if (!event.isCancelled()) {
			if (event.getChange() > maxHealth) {
				this.health = maxHealth;
			} else {
				this.health = event.getChange();
			}
		}
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}
}
