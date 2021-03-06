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

package org.orecruncher.dsurround.client.keyboard;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.hud.LightLevelHUD;
import org.orecruncher.dsurround.client.hud.LightLevelHUD.Mode;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.compat.EntityRendererUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler {

	private static final String SECTION_NAME = ModInfo.MOD_NAME;

	private static KeyBinding SELECTIONBOX_KEY;
	private static KeyBinding LIGHTLEVEL_KEY;
	private static KeyBinding CHUNKBORDER_KEY;
	public static KeyBinding ANIMANIA_BADGES;

	public static void init() {
		SELECTIONBOX_KEY = new KeyBinding("dsurround.cfg.keybind.SelectionBox", Keyboard.KEY_B, SECTION_NAME);
		ClientRegistry.registerKeyBinding(SELECTIONBOX_KEY);

		if (ModEnvironment.Animania.isLoaded()) {
			ANIMANIA_BADGES = new KeyBinding("dsurround.cfg.keybind.AnimaniaBadges", Keyboard.KEY_NONE, SECTION_NAME);
			ClientRegistry.registerKeyBinding(ANIMANIA_BADGES);
		}

		LIGHTLEVEL_KEY = new KeyBinding("dsurround.cfg.keybind.LightLevel", Keyboard.KEY_F7, SECTION_NAME);
		ClientRegistry.registerKeyBinding(LIGHTLEVEL_KEY);

		CHUNKBORDER_KEY = new KeyBinding("dsurround.cfg.keybind.ChunkBorders", Keyboard.KEY_F9, SECTION_NAME);
		ClientRegistry.registerKeyBinding(CHUNKBORDER_KEY);
	}

	private static String getOnOff(final boolean flag) {
		return Localization.format(flag ? "dsurround.cfg.keybind.msg.ON" : "dsurround.cfg.keybind.msg.OFF");
	}

	private static final String chatPrefix = TextFormatting.BLUE + "[" + TextFormatting.GREEN + ModInfo.MOD_NAME
			+ TextFormatting.BLUE + "] " + TextFormatting.RESET;

	private static void sendPlayerMessage(final String fmt, final Object... parms) {
		if (ModOptions.general.hideChatNotices)
			return;

		final EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null) {
			final String txt = chatPrefix + Localization.format(fmt, parms);
			player.sendMessage(new TextComponentString(txt));
		}
	}

	private static boolean shouldHandle(@Nonnull final KeyBinding binding) {
		return binding != null && binding.isPressed();
	}

	@SubscribeEvent(receiveCanceled = false)
	public static void onKeyboard(@Nonnull InputEvent.KeyInputEvent event) {

		if (shouldHandle(SELECTIONBOX_KEY)) {
			final EntityRenderer renderer = Minecraft.getMinecraft().entityRenderer;
			final boolean result = !EntityRendererUtil.getDrawBlockOutline(renderer);
			EntityRendererUtil.setDrawBlockOutline(renderer, result);
			sendPlayerMessage("dsurround.cfg.keybind.msg.Fencing", getOnOff(result));
		}

		if (shouldHandle(CHUNKBORDER_KEY)) {
			final boolean result = Minecraft.getMinecraft().debugRenderer.toggleChunkBorders();
			sendPlayerMessage("dsurround.cfg.keybind.msg.ChunkBorder", getOnOff(result));
		}

		if (shouldHandle(LIGHTLEVEL_KEY)) {
			if (GuiScreen.isCtrlKeyDown()) {
				// Only change mode when visible
				if (LightLevelHUD.showHUD) {
					ModOptions.huds.lightlevel.llDisplayMode++;
					if (ModOptions.huds.lightlevel.llDisplayMode >= Mode.values().length)
						ModOptions.huds.lightlevel.llDisplayMode = 0;
					sendPlayerMessage("dsurround.cfg.keybind.msg.LLDisplayMode",
							Mode.getMode(ModOptions.huds.lightlevel.llDisplayMode).name());
				}
			} else if (GuiScreen.isShiftKeyDown()) {
				if (LightLevelHUD.showHUD) {
					ModOptions.huds.lightlevel.llHideSafe = !ModOptions.huds.lightlevel.llHideSafe;
					sendPlayerMessage("dsurround.cfg.keybind.msg.LLSafeBlocks",
							getOnOff(ModOptions.huds.lightlevel.llHideSafe));
				}
			} else {
				LightLevelHUD.showHUD = !LightLevelHUD.showHUD;
				sendPlayerMessage("dsurround.cfg.keybind.msg.LLDisplay", getOnOff(LightLevelHUD.showHUD));
			}
		}

	}

}
