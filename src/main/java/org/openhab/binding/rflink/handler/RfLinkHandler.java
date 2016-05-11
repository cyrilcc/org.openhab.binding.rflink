/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.internal.DeviceMessageListener;
import org.openhab.binding.rflink.messages.RfLinkEnergyMessage;
import org.openhab.binding.rflink.messages.RfLinkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RfLinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkHandler extends BaseThingHandler implements DeviceMessageListener {

    private Logger logger = LoggerFactory.getLogger(RfLinkHandler.class);

    private RfLinkBridgeHandler bridgeHandler;

    private RfLinkDeviceConfiguration config;

    public RfLinkHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (bridgeHandler != null) {

            // TODO forge a message to be transmitted

            // bridgeHandler.sendMessage(msg);

            logger.warn("RFLink doesn't support transmitting for channel '{}' yet", channelUID.getId());

        }

    }

    @Override
    public void initialize() {
        config = getConfigAs(RfLinkDeviceConfiguration.class);

        logger.debug("Initialized RFLink device handler for {}, deviceId={}", getThing().getUID(), config.deviceId);

        initializeBridge(getBridge().getHandler(), getBridge());
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        initializeBridge(thingHandler, bridge);
    }

    private void initializeBridge(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("RFLink Bridge initialized");

        if (thingHandler != null && bridge != null) {
            bridgeHandler = (RfLinkBridgeHandler) thingHandler;
            bridgeHandler.registerDeviceStatusListener(this);

            if (bridge.getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }

        super.bridgeHandlerInitialized(thingHandler, bridge);
    }

    @Override
    public void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("RFLink Bridge disposed");
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDeviceStatusListener(this);
        }
        bridgeHandler = null;
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        super.dispose();
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RfLinkMessage message) {

        try {
            String id = message.getDeviceId();
            logger.debug("Message fom bridge " + bridge.toString() + " from device [" + id + "]");
            if (config.deviceId.equals(id)) {

                updateStatus(ThingStatus.ONLINE);

                if (message instanceof RfLinkEnergyMessage) {
                    logger.debug("Message is RfLinkEnergyMessage");
                    updateState(new ChannelUID(getThing().getUID(), RfLinkBindingConstants.CHANNEL_INSTANT_POWER),
                            new DecimalType(((RfLinkEnergyMessage) message).instantPower));
                    updateState(new ChannelUID(getThing().getUID(), RfLinkBindingConstants.CHANNEL_TOTAL_USAGE),
                            new DecimalType(((RfLinkEnergyMessage) message).totalUsage));
                }
            }

        } catch (RfLinkException e) {

            logger.error("Error occured during message receiving", e);
        }

    }
}
