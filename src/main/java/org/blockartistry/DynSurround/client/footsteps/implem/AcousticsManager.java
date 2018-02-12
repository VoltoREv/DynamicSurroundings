/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.DynSurround.client.footsteps.implem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IOptions;
import org.blockartistry.DynSurround.client.footsteps.interfaces.ISoundPlayer;
import org.blockartistry.DynSurround.client.footsteps.system.Association;
import org.blockartistry.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.FootstepSound;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.TimeUtils;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.random.XorShiftRandom;
import org.blockartistry.lib.sound.BasicSound;
import org.blockartistry.lib.sound.SoundUtils;

import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ILibrary that can also play sounds and default footsteps.
 */
@SideOnly(Side.CLIENT)
public class AcousticsManager implements ISoundPlayer {

	private final Random RANDOM = XorShiftRandom.current();

	private final HashMap<String, IAcoustic> acoustics = new HashMap<String, IAcoustic>();
	private final ObjectArray<PendingSound> pending = new ObjectArray<PendingSound>();

	// Special sentinels for equating
	public static final IAcoustic[] EMPTY = {};
	public static final IAcoustic[] NOT_EMITTER = { new BasicAcoustic("NOT_EMITTER") };
	public static final IAcoustic[] MESSY_GROUND = { new BasicAcoustic("MESSY_GROUND") };
	public static IAcoustic[] SWIM;
	public static IAcoustic[] JUMP;
	public static IAcoustic[] SPLASH;

	public AcousticsManager() {
	}

	public void addAcoustic(@Nonnull final IAcoustic acoustic) {
		this.acoustics.put(acoustic.getAcousticName(), acoustic);
	}

	@Nullable
	public IAcoustic getAcoustic(@Nonnull final String name) {
		return this.acoustics.get(name);
	}

	public void playAcoustic(@Nonnull final EntityLivingBase location, @Nonnull final Association acousticName,
			@Nonnull final EventType event, @Nonnull final Variator var) {

		// If the sound can't be heard by the player at the keyboard, just skip
		// this part.
		if (SoundUtils.canBeHeard(location, EnvironState.getPlayerPosition())) {
			if (acousticName.getNoAssociation()) {
				playStep(location, acousticName, var);
			} else {
				playAcoustic(location, acousticName.getData(), event, var, null);
			}
		}
	}

	private void logAcousticPlay(@Nonnull final IAcoustic[] acoustics, @Nonnull final EventType event) {
		if (DSurround.log().isDebugging()) {
			final String txt = String.join(",",
					Arrays.stream(acoustics).map(IAcoustic::getAcousticName).toArray(String[]::new));
			DSurround.log().debug("Playing acoustic %s for event %s", txt, event.toString().toUpperCase());
		}
	}

	public void playAcoustic(@Nonnull final EntityLivingBase location, @Nonnull final IAcoustic[] acoustics,
			@Nonnull final EventType event, @Nonnull final Variator var, @Nullable final IOptions inputOptions) {
		if (acoustics != null) {
			logAcousticPlay(acoustics, event);
			for (int i = 0; i < acoustics.length; i++) {
				acoustics[i].playSound(this, location, event, var, inputOptions);
			}
		}
	}

	@Nonnull
	public IAcoustic[] compileAcoustics(@Nonnull final String acousticName) {
		if (acousticName.equals("NOT_EMITTER"))
			return NOT_EMITTER;
		else if (acousticName.equals("MESSY_GROUND"))
			return MESSY_GROUND;

		final IAcoustic[] result = Arrays.stream(acousticName.split(",")).map(fragment -> {
			final IAcoustic a = this.acoustics.get(fragment);
			if (a == null)
				DSurround.log().warn("Acoustic '%s' not found!", fragment);
			return a;
		}).filter(a -> {
			return a != null;
		}).toArray(IAcoustic[]::new);

		return result.length == 0 ? EMPTY : result;
	}

	@Override
	public void playStep(@Nonnull final EntityLivingBase entity, @Nonnull final Association assos,
			@Nonnull final Variator var) {
		try {
			SoundType soundType = assos.getSoundType();
			if (!assos.isLiquid() && assos.getSoundType() != null) {

				if (WorldUtils.getBlockState(entity.getEntityWorld(), assos.getPos().up())
						.getBlock() == Blocks.SNOW_LAYER) {
					soundType = MCHelper.getSoundType(Blocks.SNOW_LAYER);
				}

				actuallyPlaySound(entity, soundType.getStepSound(), soundType.getVolume(), soundType.getPitch(), var,
						true);
			}
		} catch (final Throwable t) {
			DSurround.log().error("Unable to play step sound", t);
		}
	}

	@Override
	public void playSound(@Nonnull final EntityLivingBase location, @Nonnull final SoundEvent sound, final float volume,
			final float pitch, @Nonnull final Variator var, @Nullable final IOptions options) {

		try {
			if (options != null) {
				if (options.getDelayMin() > 0 && options.getDelayMax() > 0) {
					final long delay = TimeUtils.currentTimeMillis()
							+ randAB(RANDOM, options.getDelayMin(), options.getDelayMax());
					this.pending
							.add(new PendingSound(location, sound, volume, pitch, null, delay, options.getDelayMax()));
				} else {
					actuallyPlaySound(location, sound, volume, pitch, var);
				}
			} else {
				actuallyPlaySound(location, sound, volume, pitch, var);
			}
		} catch (final Throwable t) {
			DSurround.log().error("Unable to play sound", t);
		}
	}

	protected void actuallyPlaySound(@Nonnull final EntityLivingBase entity, @Nonnull final SoundEvent sound,
			final float volume, final float pitch, @Nonnull final Variator var) {
		this.actuallyPlaySound(entity, sound, volume, pitch, var, false);
	}

	protected void actuallyPlaySound(@Nonnull final EntityLivingBase entity, @Nonnull final SoundEvent sound,
			final float volume, final float pitch, @Nonnull final Variator var, final boolean noScale) {

		try {
			final FootstepSound s = new FootstepSound(entity, sound).setVolume(volume * var.VOLUME_SCALE)
					.setPitch(pitch);
			if (noScale)
				s.setVolumeScale(BasicSound.DEFAULT_SCALE);
			SoundEffectHandler.INSTANCE.playSound(s);
		} catch (final Throwable t) {
			DSurround.log().error("Unable to play sound", t);
		}
	}

	private long randAB(@Nonnull final Random rng, final long a, final long b) {
		return a >= b ? a : a + rng.nextInt((int) (b + 1));
	}

	@Override
	@Nonnull
	public Random getRNG() {
		return RANDOM;
	}

	// TODO: This state should move to Generator since it operates
	// per generator.
	public void think(@Nonnull final Variator var) {
		final long time = TimeUtils.currentTimeMillis();

		this.pending.removeIf(sound -> {
			if (sound.getTimeToPlay() <= time) {
				if (!sound.isLate(time))
					sound.playSound(AcousticsManager.this, var);
				return true;
			}
			return false;
		});
	}

}