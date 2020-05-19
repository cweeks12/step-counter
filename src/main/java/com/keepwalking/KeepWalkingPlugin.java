package com.keepwalking;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.hooks.Callbacks;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.ClientSessionManager;
import net.runelite.client.account.SessionManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class KeepWalkingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private KeepWalkingConfig config;

	@Inject
	private SessionManager sessionManager;

	private Player currentPlayer;
	private WorldPoint previousLocation;

	private int allowedSteps;

	Timer shutdownTimer = new Timer();

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
		previousLocation = new WorldPoint(0,0,0);
		allowedSteps = 10;
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
			currentPlayer = client.getLocalPlayer();
		}
	}

	private boolean areTheSameLocation(WorldPoint previous, WorldPoint current){
		return (previous.getX() == current.getX()) && (previous.getY() == current.getY());
	}

	private int distanceDelta(WorldPoint previousLocation, WorldPoint currentLocation){
		// Calculate the actual travelled distance and store it
		int potentialDistance = Math.abs(previousLocation.getX() - currentLocation.getX()) + Math.abs(previousLocation.getY() - currentLocation.getY());

		// If it's an unreasonable distance. I don't know what an unreasonable distance is, but this should
		// account for teleports and entering the client, then we say you didn't take any steps.
		if (potentialDistance > 100){
			return 0;
		}
		return potentialDistance;
	}

//	class ShutdownTask extends TimerTask {
//		public void run(){
//			shutdownTimer.cancel();
//			client.stopNow();
//
//		}
//	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		WorldPoint currentLocation = currentPlayer.getWorldLocation();

		if (!areTheSameLocation(previousLocation, currentLocation)){

			int distanceTravelledThisTick = distanceDelta(previousLocation, currentLocation);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
					"You travelled: "+ String.valueOf(distanceTravelledThisTick) +
							"tiles.", null);

			previousLocation = currentLocation;

			allowedSteps -= distanceTravelledThisTick;

			if (allowedSteps <= 0){
				client.addChatMessage(ChatMessageType.BROADCAST, "", "YOU'RE OUT OF STEPS. THE CLIENT WILL SHUT DOWN IN 10 SECONDS", null);
				//shutdownTimer.schedule(new ShutdownTask(), 2*1000);
			}
		}
	}

	@Provides
	KeepWalkingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(KeepWalkingConfig.class);
	}
}
