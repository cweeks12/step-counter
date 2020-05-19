package com.keepwalking;

import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class KeepWalkingOverlay extends OverlayPanel {

    private final KeepWalkingConfig config;
    private final KeepWalkingPlugin plugin;

    @Inject
    KeepWalkingOverlay(KeepWalkingPlugin pluginIn, KeepWalkingConfig configIn){
        super(pluginIn);
        setPosition(OverlayPosition.TOP_RIGHT);
        this.config = configIn;
        this.plugin = pluginIn;

        //Not actually sure what this does
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OverlayManager.OPTION_CONFIGURE, "Keep Walking Overlay"));

    }

    @Override
    public Dimension render(Graphics2D graphics) {

       boolean inTrouble = this.plugin.getAllowedSteps() > 0;

       String remainingSteps = String.valueOf(this.plugin.getAllowedSteps()) + " steps (" +
                                String.valueOf(this.plugin.getAllowedSteps() / this.config.StepsPerTile()) + " tiles)";

       panelComponent.getChildren().add(TitleComponent.builder()
                .text(remainingSteps)
                .color(inTrouble ? Color.WHITE : Color.RED)
                .build());

        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth(remainingSteps) + 10,
                0));

        return super.render(graphics);
    }
}
