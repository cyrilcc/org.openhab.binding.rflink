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
package org.openhab.binding.rflink.messages;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.binding.rflink.type.RfLinkTypeUtils;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for RFLink data classes. All other data classes should extend this class.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author cartemere - review Message management. add Reverse support for Switch/RTS
 */

public abstract class RfLinkBaseMessage implements RfLinkMessage {

    private Logger logger = LoggerFactory.getLogger(RfLinkBaseMessage.class);

    public final static String FIELDS_DELIMITER = ";";
    public final static String VALUE_DELIMITER = "=";
    public final static String ID_DELIMITER = "-";

    private final static String NODE_NUMBER_FROM_GATEWAY = "20";
    private final static String NODE_NUMBER_TO_GATEWAY = "10";

    private static final String DEVICE_MASK = "00000000";

    private final static int MINIMAL_SIZE_MESSAGE = 5;

    public String rawMessage;
    private byte seqNbr = 0;
    private String deviceName;
    protected String deviceId;
    protected Boolean isCommandReversed;

    @NonNull
    Map<@NonNull String, @NonNull String> values = new HashMap<>();

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
        // Example : 20;02;RTS;ID=82e8ac;SWITCH=01;CMD=DOWN;
        // Example : 20;07;Debug;RTS P1;a729000068622e;
        if (size >= MINIMAL_SIZE_MESSAGE) {
            // first element should be "20"
            if (NODE_NUMBER_FROM_GATEWAY.equals(elements[0])) {
                seqNbr = (byte) Integer.parseInt(elements[1], 16);
                deviceName = elements[2].replaceAll("[^A-Za-z0-9_-]", "");
                // build the key>value map
                for (int i = 3; i < size; i++) {
                    String[] keyValue = elements[i].split(VALUE_DELIMITER, 2);
                    if (keyValue.length > 1) {
                        // Raw values are stored, and will be decoded by sub implementations
                        values.put(keyValue[0], keyValue[1]);
                    }
                }
                deviceId = values.get("ID");
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
    public Collection<String> keys() {
        return null;
    }

    public Map<String, String> getValues() {
        return values;
    }

    @Override
    public Map<String, State> getStates() {
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
        this.isCommandReversed = config.isCommandReversed;
    }

    @Override
    public Collection<String> buildMessages() {
        return Collections.singleton(buildMessage(getCommandSuffix()));
    }

    // to override in subClasses if needed
    public String getCommandSuffix() {
        return null;
    }

    public String buildMessage(String suffix) {
        // prepare data
        String[] deviceIdParts = this.deviceId.split(ID_DELIMITER, 2);
        String channel = deviceIdParts[0];
        String channelSuffix = null;
        if (deviceIdParts.length > 1) {
            channelSuffix = deviceIdParts[1].replaceAll(ID_DELIMITER, FIELDS_DELIMITER);
        }

        // encode the message
        StringBuilder message = new StringBuilder();
        appendToMessage(message, NODE_NUMBER_TO_GATEWAY); // To Bridge
        appendToMessage(message, this.getDeviceName()); // Protocol
        // convert channel to 8 character string, RfLink spec is a bit unclear on this, but seems to work...
        appendToMessage(message, DEVICE_MASK.substring(channel.length()) + channel);
        if (channelSuffix != null) {
            // some protocols, like X10 use multiple id parts, convert all - in deviceId to ;
            appendToMessage(message, channelSuffix.replaceAll(ID_DELIMITER, FIELDS_DELIMITER));
        }
        if (suffix != null && !suffix.isEmpty()) {
            message.append(suffix + FIELDS_DELIMITER);
        }

        logger.debug("Decoded message to be sent: {}, deviceName: {}, deviceChannel: {}, primaryId: {}", message,
                this.getDeviceName(), channel, channelSuffix);

        return message.toString();
    }

    private void appendToMessage(StringBuilder message, String element) {
        message.append(element).append(FIELDS_DELIMITER);
    }

    protected Command getEffectiveCommand(Command inputCommand) {
        if (isCommandReversed) {
            // reverse the command
            Command effectiveCommand = (Command) RfLinkTypeUtils.getAntonym(inputCommand);
            if (effectiveCommand == null) {
                // no reverse available : defaulting
                return inputCommand;
            }
            return effectiveCommand;
        }
        return inputCommand;
    }
}
