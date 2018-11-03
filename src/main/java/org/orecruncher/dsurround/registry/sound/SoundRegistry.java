/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.registry.sound;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.gui.ConfigSound;
import org.orecruncher.dsurround.client.sound.SoundLoader;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.lib.MyUtils;
import org.orecruncher.lib.math.MathStuff;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoundRegistry extends Registry {

	public static final float MIN_SOUNDFACTOR = 0F;
	public static final float MAX_SOUNDFACTOR = 4F;
	public static final float DEFAULT_SOUNDFACTOR = 1F;

	private static final String ARMOR_SOUND_PREFIX = ModBase.MOD_ID + ":fs.armor.";

	private final List<String> cullSoundNames = new ArrayList<>();
	private final List<String> blockSoundNames = new ArrayList<>();
	private final Object2FloatOpenHashMap<String> volumeControl;

	public SoundRegistry(@Nonnull final Side side) {
		super(side);
		this.volumeControl = new Object2FloatOpenHashMap<>();
		this.volumeControl.defaultReturnValue(DEFAULT_SOUNDFACTOR);
	}

	@Override
	public void init() {
		this.cullSoundNames.clear();
		this.blockSoundNames.clear();
		this.volumeControl.clear();

		MyUtils.addAll(this.cullSoundNames, ModOptions.sound.culledSounds);
		MyUtils.addAll(this.blockSoundNames, ModOptions.sound.blockedSounds);

		for (final String volume : ModOptions.sound.soundVolumes) {
			final String[] tokens = StringUtils.split(volume, "=");
			if (tokens.length == 2) {
				try {
					final float vol = Integer.parseInt(tokens[1]) / 100.0F;
					this.volumeControl.put(tokens[0], MathStuff.clamp(vol, MIN_SOUNDFACTOR, MAX_SOUNDFACTOR));
				} catch (final Throwable t) {
					ModBase.log().error("Unable to process sound volume entry: " + volume, t);
				}
			}
		}
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		// Nothing to configure
	}

	@Override
	public void fini() {

	}

	public boolean isSoundCulled(@Nonnull final String sound) {
		return this.cullSoundNames.contains(sound);
	}

	public boolean isSoundBlocked(@Nonnull final String sound) {
		return this.blockSoundNames.contains(sound);
	}

	public boolean isSoundBlockedLogical(@Nonnull final String sound) {
		return isSoundBlocked(sound) || (!ModOptions.sound.enableArmorSounds && sound.startsWith(ARMOR_SOUND_PREFIX));
	}

	public float getVolumeScale(@Nonnull final String soundName) {
		return this.volumeControl.getFloat(soundName);
	}

	public float getVolumeScale(@Nonnull final ISound sound) {
		return (sound.getSoundLocation() == null || sound instanceof ConfigSound) ? 1F
				: this.volumeControl.getFloat(sound.getSoundLocation().toString());
	}

	@Nullable
	public SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation resource) {
		return SoundLoader.getSoundMetadata(resource);
	}

}