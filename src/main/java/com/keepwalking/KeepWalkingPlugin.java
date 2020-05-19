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
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Timer;

@Slf4j
@PluginDescriptor(
	name = "Keep Walking"
)
public class KeepWalkingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private KeepWalkingConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeepWalkingOverlay overlay;

	private Player currentPlayer;
	private WorldPoint previousLocation;

	private int allowedSteps;
	private int initialGoal;
	private boolean firstLoad;

	@Override
	protected void startUp() throws Exception
	{
		previousLocation = new WorldPoint(0,0,0);
		allowedSteps = config.StepTotal();

		overlayManager.add(overlay);

		firstLoad = true;

	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		firstLoad = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			currentPlayer = client.getLocalPlayer();


			if (firstLoad){
				client.addChatMessage(ChatMessageType.BROADCAST, "", "DON'T FORGET TO CONFIGURE YOUR STEP COUNT BEFORE PLAYING.", null);
				firstLoad = false;
				allowedSteps = config.StepTotal();
				initialGoal = allowedSteps;
			}


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

		// Multiply by configured steps per tile before returning.
		return potentialDistance * config.StepsPerTile();
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{

		if (initialGoal != config.StepTotal()){
			allowedSteps = config.StepTotal();

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
					"Steps available updated to " + String.valueOf(allowedSteps), null);

			initialGoal = config.StepTotal();
		}
		WorldPoint currentLocation = currentPlayer.getWorldLocation();

		if (!areTheSameLocation(previousLocation, currentLocation)){

			int distanceTravelledThisTick = distanceDelta(previousLocation, currentLocation);

			previousLocation = currentLocation;

			allowedSteps -= distanceTravelledThisTick;

			if (allowedSteps <= 0){
				client.addChatMessage(ChatMessageType.BROADCAST, "", "YOU'RE OUT OF STEPS. PLEASE LOG OUT.", null);
			}
		}
	}

	@Provides
	KeepWalkingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(KeepWalkingConfig.class);
	}

	public int getAllowedSteps(){
		return allowedSteps;
	}
}
