/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.binding.rflink.type.RfLinkTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RfLink data class for power switch message.
 *
 * @author Daan Sieben - Initial contribution
 * @author John Jore - Added channel for Contacts
 * @author Arjan Mels - Simplified by using system OnOffType and OpenClosedType
 * @author John Jore - Simplification breaks "Contacts" as RfLink outputs OFF/ON, not OPEN/CLOSED. Reverted
 */
public class RfLinkSwitchMessage extends RfLinkBaseMessage {
    private static final String KEY_SWITCH = "SWITCH";
    private static final String KEY_CMD = "CMD";
    private static final String VALUE_DIMMING_PREFIX = "SET_LEVEL";

    private static final Collection<String> KEYS = Arrays.asList(KEY_SWITCH, KEY_CMD);
    private static Logger logger = LoggerFactory.getLogger(RfLinkSwitchMessage.class);

    public Type command = OnOffType.OFF;
    public Type contact = OpenClosedType.CLOSED;
    public Type dimming = null;

    public RfLinkSwitchMessage() {
    }

    public RfLinkSwitchMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_SWITCH;
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Command = " + command;
        str += ", Contact = " + contact;
        if (dimming != null) {
            str += ", Dimming=" + dimming;
        }
        return str;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);

        if (values.containsKey(KEY_CMD)) {
            command = RfLinkTypeUtils.getTypeFromStringValue(values.get(KEY_CMD));
            if (RfLinkTypeUtils.isNullOrUndef(command)) {
                // no explicit command set, try to parse Dimming
                Integer dimmingValue = getDimmingValue(values.get(KEY_CMD));
                if (dimmingValue != null) {
                    dimming = new DecimalType(dimmingValue);
                    command = RfLinkTypeUtils.getOnOffCommandFromDimming((DecimalType) dimming);
                }
            }
            contact = RfLinkTypeUtils.getSynonym(command, OpenClosedType.class);
        }

        if (values.containsKey(KEY_SWITCH)) {
            this.deviceId += ID_DELIMITER + values.get(KEY_SWITCH);
        }
    }

    private Integer getDimmingValue(String value) {
        if (isDimmingValue(value)) {
            String[] valueElements = value.trim().split("=");
            if (valueElements.length > 1) {
                try {
                    return Integer.valueOf(valueElements[1]);
                } catch (NumberFormatException ex) {
                    logger.error("Could not parse DimmingValue for : {}", value, ex);
                }
            }
        }
        return null;
    }

    private boolean isDimmingValue(String value) {
        if (value != null && value.contains(VALUE_DIMMING_PREFIX + RfLinkBaseMessage.VALUE_DELIMITER)) {
            return true;
        }
        return false;
    }

    @Override
    public Collection<String> keys() {
        return KEYS;
    }

    @Override
    public Map<String, State> getStates() {
        Map<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_COMMAND, ((State) RfLinkTypeUtils.getSynonym(command, OnOffType.class)));
        map.put(RfLinkBindingConstants.CHANNEL_CONTACT, (State) contact);
        if (dimming != null) {
            map.put(RfLinkBindingConstants.CHANNEL_DIMMING_LEVEL, (State) dimming);
        }
        return map;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);
        initializeCommandFromTriggeredCommand(triggeredCommand);
    }

    private void initializeCommandFromTriggeredCommand(Command triggeredCommand) {
        Command convertedCommand = triggeredCommand;
        if (triggeredCommand instanceof PercentType) {
            convertedCommand = RfLinkTypeUtils.toDecimalType((PercentType) triggeredCommand, 0, 15);
        }
        if (convertedCommand instanceof DecimalType) {
            DecimalType decimalCommand = RfLinkTypeUtils.boundDecimal((DecimalType) convertedCommand, 0, 15);
            dimming = decimalCommand;
            command = RfLinkTypeUtils.getOnOffCommandFromDimming(decimalCommand);
        } else {
            command = RfLinkTypeUtils.getSynonym(convertedCommand, OnOffType.class);
        }
        contact = RfLinkTypeUtils.getSynonym(command, OpenClosedType.class);
    }

    @Override
    public String getCommandSuffix() {
        if (dimming != null && ((DecimalType) dimming).intValue() > 0) {
            return dimming.toFullString();
        }
        return command.toFullString();
    }

}
