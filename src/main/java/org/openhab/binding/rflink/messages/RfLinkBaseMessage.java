/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for RFLink data classes. All other data classes should extend this class.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public abstract class RfLinkBaseMessage implements RfLinkMessage {

    private Logger logger = LoggerFactory.getLogger(RfLinkBaseMessage.class);

    protected final static String FIELDS_DELIMITER = ";";
    protected final static char VALUE_DELIMITER = '=';
    protected final static String STR_VALUE_DELIMITER = "=";
    public final static String ID_DELIMITER = "-";

    private final static String NODE_NUMBER_FROM_GATEWAY = "20";

    private final static int MINIMAL_SIZE_MESSAGE = 5;

    public String rawMessage;

    public byte seqNbr = 0;

    private String deviceName;

    protected String deviceId;

    protected HashMap<String, String> values = new HashMap<>();

    public RfLinkBaseMessage() {

    }

    public RfLinkBaseMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return null;
    }

    @Override
    public void encodeMessage(String data) {

        rawMessage = data;

        final String[] elements = rawMessage.split(FIELDS_DELIMITER);
        final int size = elements.length;

        // Every message should have at least 5 parts
        // Example : 20;31;Mebus;ID=c201;TEMP=00cf;
        // Example : 20;07;Debug;RTS P1;a729000068622e;
        if (size >= MINIMAL_SIZE_MESSAGE) {
            // first element should be "20"
            if (NODE_NUMBER_FROM_GATEWAY.equals(elements[0])) {

                seqNbr = (byte) Integer.parseInt(elements[1], 16);
                deviceName = elements[2].replaceAll("[^A-Za-z0-9_-]", "");

                if ((elements[3].indexOf(STR_VALUE_DELIMITER)) > -1) { // can return "Debug" as ID
                    deviceId = elements[3].split(STR_VALUE_DELIMITER)[1];

                    // Raw values are stored, and will be decoded by sub implementations
                    for (int i = 4; i < size; i++) {
                        // we don't use split() method since values can have '=' in it
                        final int idx = elements[i].indexOf(VALUE_DELIMITER);
                        if (idx > -1) { // can return "BAD_CRC?" in values
                            final String name = elements[i].substring(0, idx);
                            final String value = elements[i].substring(idx + 1, elements[i].length());

                            values.put(name, value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {

        String str = "";

        if (rawMessage == null) {
            str += "Raw data = unknown";
        } else {
            str += "Raw data = " + new String(rawMessage);
            str += ", Seq number = " + (short) (seqNbr & 0xFF);
            str += ", Device name = " + deviceName;
            str += ", Device ID = " + deviceId;
        }

        return str;
    }

    @Override
    public String getDeviceId() {
        return deviceName + ID_DELIMITER + deviceId;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * return a list of keys that are present in the message to be decoded
     */
    @Override
    public List<String> keys() {
        return null;
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    @Override
    public HashMap<String, State> getStates() {
        return null;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException, RfLinkException {
        String[] elements = config.deviceId.split(ID_DELIMITER);
        if (elements.length >= 2) {
            this.deviceName = elements[0];
            this.deviceId = config.deviceId.substring(this.deviceName.length() + ID_DELIMITER.length());
        }
    }

    @Override
    public byte[] decodeMessage(String suffix) {
        String message = "10;"; // message for bridge

        String[] deviceIdParts = this.deviceId.split(ID_DELIMITER, 2);
        String primaryId = deviceIdParts[0];

        // convert channel to 6 character string, RfLink spec is a bit unclear on this, but seems to work...
        String deviceChannel = "000000".substring(primaryId.length()) + primaryId;
        if (deviceIdParts.length > 1) {
            deviceChannel += ID_DELIMITER + deviceIdParts[1];
        }

        message += this.getDeviceName() + ";";
        // some protocols, like X10 use multiple id parts, convert all - in deviceId to ;
        message += deviceChannel.replaceAll(ID_DELIMITER, ";") + ";";
        message += suffix;

        logger.debug("Decoded message to be sent: {}, deviceName: {}, deviceChannel: {}, primaryId: {}", message,
                this.getDeviceName(), deviceChannel, primaryId);

        message += "\n"; // close message with newline

        return message.getBytes();
    }
}
