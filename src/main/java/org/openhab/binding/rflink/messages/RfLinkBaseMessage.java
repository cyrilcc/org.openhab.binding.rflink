/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;

/**
 * Base class for RFLink data classes. All other data classes should extend this class.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public abstract class RfLinkBaseMessage implements RfLinkMessage {

    protected final static String FIELDS_DELIMITER = ";";
    protected final static char VALUE_DELIMITER = '=';
    protected final static String STR_VALUE_DELIMITER = "=";
    public final static String ID_DELIMITER = "-";
    private final static String NODE_NUMBER_FROM_GATEWAY = "20";
    private final static int MINIMAL_SIZE_MESSAGE = 5;
    public String rawMessage;
    public byte seqNbr = 0;
    private String deviceName;
    private String deviceId;
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
        if (size >= MINIMAL_SIZE_MESSAGE) {
            // first element should be "20"
            if (NODE_NUMBER_FROM_GATEWAY.equals(elements[0])) {

                seqNbr = (byte) Integer.parseInt(elements[1], 16);

                // Fix for "UID segment 'Oregon Temp_0710' contains invalid characters. Each segment of the UID must
                // match the pattern [A-Za-z0-9_-]*."
                deviceName = elements[2].replaceAll("[^A-Za-z0-9_-]", "");
                deviceId = elements[3].split(STR_VALUE_DELIMITER)[1];

                // Raw values are stored, and will be decoded by sub implementations
                for (int i = 4; i < size; i++) {
                    // we don't use split() method since values can have '=' in it

                    final int idx = elements[i].indexOf(VALUE_DELIMITER);
                    final String name = elements[i].substring(0, idx);
                    final String value = elements[i].substring(idx + 1, elements[i].length());

                    values.put(name, value);
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
}
