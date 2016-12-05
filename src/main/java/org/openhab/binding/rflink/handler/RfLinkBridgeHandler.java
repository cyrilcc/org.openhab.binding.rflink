/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
 * {@link RfLinkHanlder}s use the {@link RfLinkBridgeHandler} to execute the
 * actual commands.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(RfLinkBridgeHandler.class);

    RfLinkConnectorInterface connector = null;
    private MessageListener eventListener = new MessageListener();

    private List<DeviceMessageListener> deviceStatusListeners = new CopyOnWriteArrayList<>();

    // private static final int timeout = 5000;
    private static byte seqNbr = 0;
    // private static RFXComTransmitterMessage responseMessage = null;
    // private Object notifierObject = new Object();
    private RfLinkBridgeConfiguration configuration = null;
    private ScheduledFuture<?> connectorTask;

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
            connectorTask = scheduler.scheduleAtFixedRate(new Runnable() {

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

    // private static synchronized byte getSeqNumber() {
    // return seqNbr;
    // }
    //
    // private static synchronized byte getNextSeqNumber() {
    // if (++seqNbr == 0) {
    // seqNbr = 1;
    // }
    //
    // return seqNbr;
    // }

    // private static synchronized RFXComTransmitterMessage getResponseMessage() {
    // return responseMessage;
    // }
    //
    // private static synchronized void setResponseMessage(RFXComTransmitterMessage respMessage) {
    // responseMessage = respMessage;
    // }

    private void connect() {
        logger.debug("Connecting to RFLink transceiver on " + configuration.serialPort + " port");

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
        } catch (Throwable t) {
            logger.error("RFLink error", t);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public synchronized void sendMessage(RfLinkMessage msg) throws RfLinkException {

        logger.warn("RFLink sendMessage not implemented yet");

        // ((RfLinkBaseMessage) msg).seqNbr = getNextSeqNumber();
        // byte[] data = msg.decodeMessage();
        //
        // logger.debug("Transmitting message '{}'", msg);
        // logger.trace("Transmitting data: {}", DatatypeConverter.printHexBinary(data));
        //
        // setResponseMessage(null);
        //
        // try {
        // connector.sendMessage(data);
        // } catch (IOException e) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        // throw new RfLinkException(e);
        // }
        //
        // try {
        //
        // RFXComTransmitterMessage resp = null;
        // synchronized (notifierObject) {
        // notifierObject.wait(timeout);
        // resp = getResponseMessage();
        // }
        //
        // if (resp != null) {
        // switch (resp.response) {
        // case ACK:
        // case ACK_DELAYED:
        // logger.debug("Command successfully transmitted, '{}' received", resp.response);
        // break;
        //
        // case NAK:
        // case NAK_INVALID_AC_ADDRESS:
        // case UNKNOWN:
        // logger.error("Command transmit failed, '{}' received", resp.response);
        // break;
        // }
        // } else {
        // logger.warn("No response received from transceiver");
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        // }
        //
        // } catch (InterruptedException ie) {
        // logger.error("No acknowledge received from RFLink controller, timeout {}ms ", timeout);
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        // }
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
                logger.error("Error occured during packet receiving, data: {}", packet.toString(), e.getMessage());
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

}