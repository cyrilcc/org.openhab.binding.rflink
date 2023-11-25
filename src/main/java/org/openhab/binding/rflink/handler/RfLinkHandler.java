/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rflink.handler;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.binding.rflink.internal.DeviceMessageListener;
import org.openhab.binding.rflink.messages.RfLinkMessage;
import org.openhab.binding.rflink.messages.RfLinkMessageFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RfLinkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author John Jore - Added initial support to send commands to devices
 * @author Arjan Mels - Added option to repeat messages
 */
public class RfLinkHandler extends BaseThingHandler implements DeviceMessageListener {

    public static final int TIME_BETWEEN_COMMANDS = 50;
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
            if (command instanceof RefreshType) {
                // Not supported
            } else {
                try {
                    RfLinkMessage message = RfLinkMessageFactory
                            .createMessageForSendingToThing(getThing().getThingTypeUID());
                    message.initializeFromChannel(getConfigAs(RfLinkDeviceConfiguration.class), channelUID, command);
                    updateThingStates(message);
                    int repeats = 1;
                    if (getThing().getConfiguration().containsKey("repeats")) {
                        repeats = ((BigDecimal) getThing().getConfiguration().get("repeats")).intValue();
                    }
                    repeats = Math.min(Math.max(repeats, 1), 20);
                    for (int i = 0; i < repeats; i++) {
                        bridgeHandler.sendMessage(message);
                    }
                } catch (RfLinkNotImpException e) {
                    logger.error("Message not supported: {}", e.getMessage());
                } catch (RfLinkException e) {
                    logger.error("Transmitting error: {}", e.getMessage());
                }
            }
        }
    }

    /**
     */
    @Override
    public void initialize() {
        config = getConfigAs(RfLinkDeviceConfiguration.class);
        logger.debug("Initializing thing {}, deviceId={}", getThing().getUID(), config.deviceId);
        Bridge currentBridge = getBridge();
        if (currentBridge == null) {
            initializeBridge(null, null);
        } else {
            initializeBridge(currentBridge.getHandler(), currentBridge.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge currentBridge = getBridge();
        if (currentBridge == null) {
            initializeBridge(null, bridgeStatusInfo.getStatus());
        } else {
            initializeBridge(currentBridge.getHandler(), bridgeStatusInfo.getStatus());
        }
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
            // logger.debug("Matching Message from bridge {} from device [{}] with [{}]", bridge.toString(), id,
            // config.deviceId);
            if (config.deviceId.equals(id)) {
                logger.debug("Message from bridge {} from device [{}] type [{}] matched", bridge.toString(), id,
                        message.getClass().getSimpleName());
                updateStatus(ThingStatus.ONLINE);
                updateThingStates(message);

            }

        } catch (RfLinkException e) {
            logger.error("Error occured during message receiving", e);
        }
    }

    private void updateThingStates(RfLinkMessage message) {
        @NonNull
        Map<@NonNull String, @NonNull State> map = message.getStates();
        for (String channel : map.keySet()) {
            logger.debug("Update channel: {}, state: {}", channel, map.get(channel));

            State stt = map.get(channel);
            if (stt != null) {
                updateState(new ChannelUID(getThing().getUID(), channel), stt);
            }
        }
    }
}
