/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.handler;

import java.util.HashMap;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.internal.DeviceMessageListener;
import org.openhab.binding.rflink.messages.RfLinkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RfLinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author John Jore - Added initial support to send commands to devices
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

            // there must be a better way?
            String[] tmp = channelUID.getAsString().split(":")[3].split("-");
            String protocol = tmp[0];

            // RfLink needs to know which protocol to use. Different devices have different formats.
            String msg = null;
            switch (protocol.toUpperCase()) {
                case "RTS":
                    msg = protocol + ";" + tmp[1] + ";0;" + command + ";";
                    break;
                case "X10":
                    msg = protocol + ";" + tmp[1] + ";" + tmp[2] + ";" + command + ";";
                    break;
            }

            try {
                if (msg != null) {
                    bridgeHandler.sendMessage(msg.toUpperCase());
                }
            } catch (RfLinkException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        config = getConfigAs(RfLinkDeviceConfiguration.class);

        logger.debug("Initializing thing {}, deviceId={}", getThing().getUID(), config.deviceId);

        initializeBridge((getBridge() == null) ? null : getBridge().getHandler(),
                (getBridge() == null) ? null : getBridge().getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeBridge((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeBridge(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        config = getConfigAs(RfLinkDeviceConfiguration.class);
        if (config.deviceId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "RFLink device missing deviceId");
        } else if (thingHandler != null && bridgeStatus != null) {
            bridgeHandler = (RfLinkBridgeHandler) thingHandler;
            bridgeHandler.registerDeviceStatusListener(this);

            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

        // super.bridgeHandlerInitialized(thingHandler, bridge);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDeviceStatusListener(this);
        }
        bridgeHandler = null;
        super.dispose();
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RfLinkMessage message) {

        try {
            String id = message.getDeviceId();
            logger.debug(
                    "Message fom bridge " + bridge.toString() + " from device [" + id + "], attempting to match {}",
                    config.deviceId);
            if (config.deviceId.equals(id)) {
                updateStatus(ThingStatus.ONLINE);

                HashMap<String, State> map = message.getStates();

                for (String channel : map.keySet()) {
                    updateState(new ChannelUID(getThing().getUID(), channel), map.get(channel));
                }

            }

        } catch (RfLinkException e) {

            logger.error("Error occured during message receiving", e);
        }
    }
}
