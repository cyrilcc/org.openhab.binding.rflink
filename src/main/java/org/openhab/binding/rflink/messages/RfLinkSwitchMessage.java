/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * RfLink data class for power switch message.
 *
 * @author Daan Sieben - Initial contribution
 * @author John Jore - Added channel for Contacts
 * @author Arjan Mels - Simplified by using system OnOffType and OpenClosedType
 */
public class RfLinkSwitchMessage extends RfLinkBaseMessage {
    private static final String KEY_SWITCH = "SWITCH";
    private static final String KEY_CMD = "CMD";

    private static final List<String> keys = Arrays.asList(KEY_SWITCH, KEY_CMD);

    public OnOffType command = OnOffType.OFF;
    public OpenClosedType contact = OpenClosedType.CLOSED;

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
        String str = "";

        str += super.toString();
        str += ", Command = " + command;
        str += ", Contact = " + contact;

        return str;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);

        if (values.containsKey(KEY_CMD)) {
            try {
                command = OnOffType.valueOf(values.get(KEY_CMD));
                if (command == null) {
                    throw new RfLinkException("Can't convert " + values.get(KEY_CMD) + " to Switch Command");
                }
            } catch (Exception e) {
                command = null;
            }

            try {
                contact = OpenClosedType.valueOf(values.get(KEY_CMD));
                if (contact == null) {
                    throw new RfLinkException("Can't convert " + values.get(KEY_CMD) + " to Contact state");
                }
            } catch (Exception e) {
                contact = null;
            }

        }

        if (values.containsKey(KEY_SWITCH)) {
            this.deviceId += ID_DELIMITER + values.get(KEY_SWITCH);
        }
    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public HashMap<String, State> getStates() {

        HashMap<String, State> map = new HashMap<>();

        map.put(RfLinkBindingConstants.CHANNEL_COMMAND, command);
        map.put(RfLinkBindingConstants.CHANNEL_CONTACT, contact);

        return map;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);
        command = OnOffType.valueOf(triggeredCommand.toFullString());
        if (command == null) {
            throw new RfLinkException("Can't convert " + triggeredCommand + " to Switch Command");
        }
    }

    @Override
    public byte[] decodeMessage(String suffix) {
        return super.decodeMessage(this.command.toFullString() + ";");
    }
}
