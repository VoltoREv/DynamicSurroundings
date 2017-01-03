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

package org.blockartistry.mod.DynSurround.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.SoundRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class DynSurroundConfigGui extends GuiConfig {

	private final Configuration config = DSurround.config();

	private final ConfigElement soundElement;
	private final ConfigCategory soundCategory;

	private final ConfigElement soundVolumeElement;
	private final ConfigCategory soundVolumeCategory;

	public DynSurroundConfigGui(final GuiScreen parentScreen) {
		super(parentScreen, new ArrayList<IConfigElement>(), DSurround.MOD_ID, false, false, DSurround.MOD_NAME);
		this.titleLine2 = this.config.getConfigFile().getAbsolutePath();

		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_AURORA, ModOptions.CONFIG_AURORA_ENABLED));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_RAIN, ModOptions.CONFIG_ENABLE_BACKGROUND_THUNDER));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ALLOW_DESERT_FOG));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ENABLE_ELEVATION_HAZE));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ENABLE_BIOME_FOG));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_BIOME_SOUNDS));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_JUMP_SOUND));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_SWING_SOUND));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_CRAFTING_SOUND));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_BOW_PULL_SOUND));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_FOOTSTEPS_SOUND));
		this.configElements
				.add(getPropertyConfigElement(ModOptions.CATEGORY_POTION_HUD, ModOptions.CONFIG_POTION_HUD_ENABLE));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES,
				ModOptions.CONFIG_OPTION_ENABLE_SPEECHBUBBLES));
		this.configElements.add(
				getPropertyConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES, ModOptions.CONFIG_OPTION_ENABLE_EMOJIS));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES,
				ModOptions.CONFIG_OPTION_ENABLE_ENTITY_CHAT));

		this.soundCategory = new ConfigCategory("Blocked Sounds");
		this.soundCategory.setComment("Sounds that will be blocked from playing");
		this.soundCategory.setLanguageKey("cfg.sound.BlockedSounds");
		this.soundElement = new MyConfigElement(this.soundCategory);
		generateSoundList(this.soundCategory);
		this.configElements.add(this.soundElement);

		this.soundVolumeCategory = new ConfigCategory("Sound Volumes");
		this.soundVolumeCategory.setComment("Individual sound volume control");
		this.soundVolumeCategory.setLanguageKey("cfg.sound.SoundVolumes");
		this.soundVolumeElement = new MyConfigElement(this.soundVolumeCategory);
		generateSoundVolumeList(this.soundVolumeCategory);
		this.configElements.add(this.soundVolumeElement);

		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_ASM));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_GENERAL));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_RAIN));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_FOG));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_AURORA));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_BLOCK));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_BIOMES));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_SOUND));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_PLAYER));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_LOGGING_CONTROL));
	}

	private ConfigElement getCategoryConfigElement(final String category) {
		final ConfigCategory cat = this.config.getCategory(category);
		return new ConfigElement(cat);
	}

	private ConfigElement getPropertyConfigElement(final String category, final String property) {
		final Property prop = this.config.getCategory(category).get(property);
		return new ConfigElement(prop);
	}

	@Override
	protected void actionPerformed(final GuiButton button) {

		super.actionPerformed(button);

		// Done button was pressed
		if (button.id == 2000) {
			saveSoundList();
			saveSoundVolumeList();
			this.config.save();
			ConfigProcessor.process(this.config, ModOptions.class);
			RegistryManager.reloadResources();
		}
	}

	protected void saveSoundList() {
		final List<String> sounds = new ArrayList<String>();
		for (final Entry<String, Property> entry : this.soundCategory.entrySet()) {
			if (entry.getValue().getBoolean())
				sounds.add(entry.getKey());
		}

		final String[] results = sounds.toArray(new String[sounds.size()]);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_BLOCKED_SOUNDS).set(results);
	}

	protected void saveSoundVolumeList() {
		final List<String> sounds = new ArrayList<String>();
		for (final Entry<String, Property> entry : this.soundVolumeCategory.entrySet()) {
			final int value = entry.getValue().getInt();
			if (value != 100)
				sounds.add(entry.getKey() + "=" + value);
		}

		final String[] results = sounds.toArray(new String[sounds.size()]);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_SOUND_VOLUMES).set(results);
	}

	protected void generateSoundList(final ConfigCategory cat) {
		cat.setRequiresMcRestart(false);
		cat.setRequiresWorldRestart(false);

		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		final List<String> sounds = new ArrayList<String>();
		for (final Object resource : handler.soundRegistry.getKeys())
			sounds.add(resource.toString());
		Collections.sort(sounds);

		final SoundRegistry registry = RegistryManager.get(RegistryType.SOUND);
		for (final String sound : sounds) {
			final Property prop = new Property(sound, "false", Property.Type.BOOLEAN);
			prop.setDefaultValue(false);
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.set(registry.isSoundBlocked(sound));
			cat.put(sound, prop);
		}
	}

	protected void generateSoundVolumeList(final ConfigCategory cat) {
		cat.setRequiresMcRestart(false);
		cat.setRequiresWorldRestart(false);

		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		final List<String> sounds = new ArrayList<String>();
		for (final Object resource : handler.soundRegistry.getKeys())
			sounds.add(resource.toString());
		Collections.sort(sounds);

		final SoundRegistry registry = RegistryManager.get(RegistryType.SOUND);
		for (final String sound : sounds) {
			final Property prop = new Property(sound, "100", Property.Type.INTEGER);
			prop.setMinValue(0);
			prop.setMaxValue(200);
			prop.setDefaultValue(100);
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.set(MathHelper.floor_float(registry.getVolumeScale(sound) * 100));
			prop.setConfigEntryClass(NumberSliderEntry.class);
			cat.put(sound, prop);
		}
	}
}
