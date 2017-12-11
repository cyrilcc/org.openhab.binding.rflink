/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
 */
public class RfLinkSwitchMessage extends RfLinkBaseMessage {

    private static final String KEY_SWITCH = "SWITCH";
    private static final String KEY_CMD = "CMD";

    private static final List<String> keys = Arrays.asList(KEY_SWITCH, KEY_CMD);

    public Commands command = Commands.OFF;
    public Contacts contact = Contacts.CLOSED;

    public enum Commands {
        OFF("OFF", OnOffType.OFF),
        ON("ON", OnOffType.ON),

        UNKNOWN("", null);

        private final String command;
        private final OnOffType onOffType;

        Commands(String command, OnOffType onOffType) {
            this.command = command;
            this.onOffType = onOffType;
        }

        public String getText() {
            return this.command;
        }

        public OnOffType getOnOffType() {
            return this.onOffType;
        }

        public static Commands fromString(String text) {
            if (text != null) {
                for (Commands c : Commands.values()) {
                    if (text.equalsIgnoreCase(c.command)) {
                        return c;
                    }
                }
            }
            return null;
        }

        public static Commands fromCommand(Command command) {
            if (command != null) {
                for (Commands c : Commands.values()) {
                    if (command == c.onOffType) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

    public enum Contacts {
        CLOSED("OFF", OpenClosedType.CLOSED),
        OPEN("ON", OpenClosedType.OPEN),
        UNKNOWN("", null);

        private final String contact;
        private final OpenClosedType openClosedType;

        Contacts(String contact, OpenClosedType openClosedType) {
            this.contact = contact;
            this.openClosedType = openClosedType;
        }

        public String getText() {
            return this.contact;
        }

        public OpenClosedType getOpenClosedType() {
            return this.openClosedType;
        }

        public static Contacts fromString(String text) {
            if (text != null) {
                for (Contacts c : Contacts.values()) {
                    if (text.equalsIgnoreCase(c.contact)) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

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
                command = Commands.fromString(values.get(KEY_CMD));
                if (command == null) {
                    throw new RfLinkException("Can't convert " + values.get(KEY_CMD) + " to Switch Command");
                }
            } catch (Exception e) {
                command = Commands.UNKNOWN;
            }

            try {
                contact = Contacts.fromString(values.get(KEY_CMD));
                if (contact == null) {
                    throw new RfLinkException("Can't convert " + values.get(KEY_CMD) + " to Contact state");
                }
            } catch (Exception e) {
                contact = Contacts.UNKNOWN;
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

        if (this.command.getOnOffType() != null) {
            map.put(RfLinkBindingConstants.CHANNEL_COMMAND, this.command.getOnOffType());
        }

        if (this.contact.getOpenClosedType() != null) {
            map.put(RfLinkBindingConstants.CHANNEL_CONTACT, this.contact.getOpenClosedType());
        }
        return map;

    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);
        command = Commands.fromCommand(triggeredCommand);
        if (command == null) {
            throw new RfLinkException("Can't convert " + triggeredCommand + " to Lighting Command");
        }
    }

    @Override
    public byte[] decodeMessage(String suffix) {
        return super.decodeMessage(this.command.getText() + ";");
    }
}
