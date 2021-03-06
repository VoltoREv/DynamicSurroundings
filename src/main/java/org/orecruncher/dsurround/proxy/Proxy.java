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

package org.orecruncher.dsurround.proxy;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.capabilities.CapabilityDimensionInfo;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;
import org.orecruncher.dsurround.commands.CommandDS;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.dsurround.network.Network;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.server.services.ServiceManager;
import org.orecruncher.lib.Localization;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class Proxy {

	protected long connectionTime = 0;

	protected void registerLanguage() {
		Localization.initialize(Side.SERVER, ModInfo.MOD_ID);
	}

	protected static void register(final Class<?> clazz) {
		ModBase.log().debug("Registering for Forge events: %s", clazz.getName());
		MinecraftForge.EVENT_BUS.register(clazz);
	}

	protected void eventBusRegistrations() {
		register(CapabilityEntityData.EventHandler.class);
		register(RegistryManager.class);
	}

	public long currentSessionDuration() {
		return System.currentTimeMillis() - this.connectionTime;
	}

	public boolean isRunningAsServer() {
		return true;
	}

	public Side effectiveSide() {
		return Side.SERVER;
	}

	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		registerLanguage();
		eventBusRegistrations();

		CapabilityEntityData.register();
		CapabilityDimensionInfo.register();
	}

	public void init(@Nonnull final FMLInitializationEvent event) {
		ModEnvironment.initialize();
		Network.initialize();
	}

	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		// Intentionally left blank
	}

	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
		RegistryManager.initialize();
	}

	public void clientConnect(@Nonnull final ClientConnectedToServerEvent event) {
		// NOTHING SHOULD BE HERE - OVERRIDE IN ProxyClient!
	}

	public void clientDisconnect(@Nonnull final ClientDisconnectionFromServerEvent event) {
		// NOTHING SHOULD BE HERE - OVERRIDE IN ProxyClient!
	}

	public void serverAboutToStart(@Nonnull final FMLServerAboutToStartEvent event) {
		ServiceManager.initialize();
	}

	public void serverStarting(@Nonnull final FMLServerStartingEvent event) {
		final MinecraftServer server = event.getServer();
		final ICommandManager command = server.getCommandManager();
		final ServerCommandManager serverCommand = (ServerCommandManager) command;
		serverCommand.registerCommand(new CommandDS());
	}

	public void serverStopping(@Nonnull final FMLServerStoppingEvent event) {
		// Intentionally left blank
	}

	public void serverStopped(@Nonnull final FMLServerStoppedEvent event) {
		ServiceManager.deinitialize();
	}

	/**
	 * Force a proxy to pick a side in this fight
	 *
	 * @param context
	 * @return
	 */
	public IThreadListener getThreadListener(@Nonnull final MessageContext context) {
		if (context.side.isServer()) {
			return context.getServerHandler().player.getServer();
		} else {
			throw new IllegalStateException(
					"Tried to get the IThreadListener from a client-side MessageContext on the dedicated server");
		}
	}

}
