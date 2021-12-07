/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.rflink.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.core.io.transport.serial.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFLink connector for serial port communication.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author Arjan Mels - Added 200ms pause between messages to improve communication
 */
public class RfLinkSerialConnector implements RfLinkConnectorInterface, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RfLinkSerialConnector.class);

    private static List<RfLinkEventListener> _listeners = new ArrayList<RfLinkEventListener>();

    SerialPort serialPort;
    private @NonNullByDefault({}) SerialPortIdentifier portIdentifier;
    private final SerialPortManager serialPortManager;

    /*
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;

    // delay between messages
    private static final int SEND_DELAY = 50;

    private static long lastSend = 0;

    @Activate
    public RfLinkSerialConnector(final @Reference SerialPortManager serialPortManager) {

        logger.debug("RfLinkRxTxConnector()");
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void connect(String comPort, int baudRate) throws Exception {

        logger.debug("connect({})", comPort);

        portIdentifier = serialPortManager.getIdentifier(comPort);
        if (portIdentifier == null) {
            logger.debug("Serial Error: Port {} does not exist.", comPort);
            return;
        }
        serialPort = portIdentifier.open("org.openhab.binding.rflink", 100);
        // set port parameters
        serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.disableReceiveTimeout();
        serialPort.enableReceiveThreshold(1);

        // open the streams
        input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        output = serialPort.getOutputStream();
        output.flush();

        try {
            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            logger.error("{}", e.toString());
            sendErrorToListeners("Unhandled exception " + e.toString());
        }
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting");

        if (serialPort != null) {
            serialPort.removeEventListener();
            logger.debug("Serial port event listener stopped");
        }

        if (output != null) {
            logger.debug("Close serial out stream");
            try {
                output.close();
            } catch (IOException e) {
                logger.trace("cannot close output stream");
            }
        }
        if (input != null) {
            logger.debug("Close serial in stream");
            try {
                input.close();
            } catch (IOException e) {
                logger.trace("cannot close input stream");
            }
        }

        if (serialPort != null) {
            logger.debug("Close serial port");
            serialPort.close();
        }

        serialPort = null;
        output = null;
        input = null;

        logger.debug("Closed");
    }

    @Override
    public void sendMessages(Collection<String> messages) throws IOException {
        if (output == null) {
            throw new IOException("Not connected, sending messages is not possible");
        }

        synchronized (this) {

            for (String message : messages) {
                long towait = SEND_DELAY - (System.currentTimeMillis() - lastSend);
                towait = Math.min(Math.max(towait, 0), SEND_DELAY);

                byte[] messageData = (message + RfLinkBindingConstants.NEW_LINE).getBytes();
                logger.debug("Send data (after {}ms, len={}): {}", towait, messageData.length,
                        DatatypeConverter.printHexBinary(messageData));
                if (towait > 0) {
                    try {
                        Thread.sleep(towait);
                    } catch (InterruptedException ignore) {
                    }
                }

                output.write(messageData);
                output.flush();
                lastSend = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void addEventListener(RfLinkEventListener listener) {
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    @Override
    public void removeEventListener(RfLinkEventListener listener) {
        _listeners.remove(listener);
    }

    private void sendMsgToListeners(String msg) {
        try {
            Iterator<RfLinkEventListener> iterator = _listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().packetReceived(msg);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    private void sendErrorToListeners(String error) {
        try {
            Iterator<RfLinkEventListener> iterator = _listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().errorOccured(error);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();
                logger.debug("<<< {}", inputLine);
                sendMsgToListeners(inputLine);
            } catch (Exception e) {
                logger.error("{}", e.toString());
            }
        }
    }
}
