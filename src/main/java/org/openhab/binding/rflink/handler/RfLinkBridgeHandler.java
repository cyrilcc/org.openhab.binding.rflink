/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.handler;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rflink.config.RfLinkBridgeConfiguration;
import org.openhab.binding.rflink.connector.RfLinkConnectorInterface;
import org.openhab.binding.rflink.connector.RfLinkEventListener;
import org.openhab.binding.rflink.connector.RfLinkSerialConnector;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.binding.rflink.internal.DeviceMessageListener;
import org.openhab.binding.rflink.messages.RfLinkMessage;
import org.openhab.binding.rflink.messages.RfLinkMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RfLinkBridgeHandler} is the handler for a RFLink transceivers. All
 * {@link RfLinkHandler}s use the {@link RfLinkBridgeHandler} to execute the
 * actual commands.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author John Jore - Added initial support to transmit messages to devices
 * @author Marvyn Zalewski - Added getConfiguration Method
 */
public class RfLinkBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(RfLinkBridgeHandler.class);

    RfLinkConnectorInterface connector = null;
    private MessageListener eventListener = new MessageListener();

    private List<DeviceMessageListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

    private RfLinkBridgeConfiguration configuration = null;
    private ScheduledFuture<?> connectorTask;

    private class TransmitQueue {
        private Queue<RfLinkMessage> queue = new LinkedBlockingQueue<RfLinkMessage>();

        public synchronized void enqueue(RfLinkMessage msg) throws IOException {
            boolean wasEmpty = queue.isEmpty();
            if (queue.offer(msg)) {
                if (wasEmpty) {
                    send();
                }
            } else {
                logger.error("Transmit queue overflow. Lost message: {}", msg);
            }
        }

        public synchronized void send() throws IOException {
            while (!queue.isEmpty()) {
                RfLinkMessage msg = queue.poll();
                logger.debug("Transmitting message '{}'", msg);
                connector.sendMessages(msg.buildMessages());
            }
        }
    }

    private TransmitQueue transmitQueue = new TransmitQueue();

    public RfLinkBridgeHandler(Bridge br) {
        super(br);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge commands not supported.");
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        if (connector != null) {
            connector.removeEventListener(eventListener);
            connector.disconnect();
        }

        if (connectorTask != null && !connectorTask.isCancelled()) {
            connectorTask.cancel(true);
            connectorTask = null;
        }

        super.dispose();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RFLink bridge handler");
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(RfLinkBridgeConfiguration.class);

        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    logger.debug("Checking RFLink transceiver connection, thing status = {}", thing.getStatus());
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        connect();
                    }
                }
            }, 0, 60, TimeUnit.SECONDS);
        }
    }

    private void connect() {
        logger.debug("Connecting to RFLink transceiver on {} port", configuration.serialPort);

        try {

            if (connector == null) {
                connector = new RfLinkSerialConnector();
            }

            if (connector != null) {
                connector.disconnect();
                connector.connect(configuration.serialPort, configuration.baudRate);
                connector.addEventListener(eventListener);
                logger.debug("RFLink receiver started");
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("connector is null");
            }
        } catch (Exception e) {
            logger.error("Connection to RFLink transceiver failed: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE);
        } catch (UnsatisfiedLinkError e) {
            logger.error("Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
                    System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public synchronized void sendMessage(RfLinkMessage msg) throws RfLinkException {
        logger.debug("sendMessage: {}", msg);

        try {
            transmitQueue.enqueue(msg);
        } catch (IOException e) {
            logger.error("I/O Error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private class MessageListener implements RfLinkEventListener {

        @Override
        public void packetReceived(String packet) {
            try {
                RfLinkMessage message = RfLinkMessageFactory.createMessage(packet);
                logger.debug("Message received: {}, running against {} listeners", message,
                        deviceStatusListeners.size());

                for (DeviceMessageListener deviceStatusListener : deviceStatusListeners) {
                    try {
                        deviceStatusListener.onDeviceMessageReceived(getThing().getUID(), message);
                    } catch (Exception e) {
                        logger.error("An exception occurred while calling the DeviceStatusListener", e);
                    }
                }

            } catch (RfLinkNotImpException e) {
                logger.debug("Message not supported, data: {}", packet.toString());
            } catch (RfLinkException e) {
                logger.error("Error occured during packet receiving, data: {}; {}", packet.toString(), e.getMessage());
            }

            updateStatus(ThingStatus.ONLINE);
        }

        @Override
        public void errorOccured(String error) {
            logger.error("Error occured: {}", error);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public boolean registerDeviceStatusListener(DeviceMessageListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.contains(deviceStatusListener) ? false
                : deviceStatusListeners.add(deviceStatusListener);
    }

    public boolean unregisterDeviceStatusListener(DeviceMessageListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    public RfLinkBridgeConfiguration getConfiguration() {
        return configuration;
    }
}
