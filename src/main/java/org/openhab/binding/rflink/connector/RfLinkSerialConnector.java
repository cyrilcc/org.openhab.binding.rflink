/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
* RFLink connector for serial port communication.
*
* @author Cyril Cauchois - Initial contribution
*/
public class RfLinkSerialConnector implements RfLinkConnectorInterface, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RfLinkSerialConnector.class);

    private static List<RfLinkEventListener> _listeners = new ArrayList<RfLinkEventListener>();

    SerialPort serialPort;

    /*
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;

    public RfLinkSerialConnector() {

        logger.debug("RfLinkRxTxConnector()");
    }

    @Override
    public void connect(String comPort, int baudRate) throws Exception {

        logger.debug("connect({})", comPort);

        // the next line is for Raspberry Pi and
        // gets us into the while loop and was suggested here was suggested
        // http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        // System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            logger.debug("Found port : {}", currPortId.getName());

            if (currPortId.getName().equals(comPort)) {
                portId = currPortId;
                break;
            }
        }
        
        if (portId == null) {
            logger.error("Could not find COM port {}", comPort);
            sendErrorToListeners("Could not find COM port " + comPort);
            throw new RfLinkException("Could not find COM port " + comPort);
        }

        // open serial port, and use class name for the appName.
        serialPort = portId.open(this.getClass().getName(), TIME_OUT);

        // set port parameters
        serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

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
            IOUtils.closeQuietly(output);
        }
        if (input != null) {
            logger.debug("Close serial in stream");
            IOUtils.closeQuietly(input);
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
    public void sendMessage(String data) throws IOException {
        if (output == null) {
            throw new IOException("Not connected, sending messages is not possible");
        }

        data = "10;" + data + "\r\n"; // Pre and Post for command. May not need both \r and \n...
        logger.debug("Send data (len={}): {}", data.length(), data);
        output.write(data.getBytes());
        output.flush();
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
