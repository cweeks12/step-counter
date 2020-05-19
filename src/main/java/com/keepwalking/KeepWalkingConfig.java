package com.keepwalking;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Keep Walking")
public interface KeepWalkingConfig extends Config
{
	@ConfigItem(
			position = 1,
		keyName = "StepsPerTile",
		name = "Steps Per Tile",
		description = "This is how many real-world steps equate to a single tile in RS."
	)
	default int StepsPerTile()
	{
		return 1;
	}

	@ConfigItem(position = 2,
	keyName = "StepTotal",
	name = "Steps Earned",
	description = "This is how many steps you're allotted when playing")
	default int StepTotal(){
		return 1000;
	}
}
