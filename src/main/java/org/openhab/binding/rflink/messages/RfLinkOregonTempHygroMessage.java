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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * RfLink data class for temperature message.
 *
 * @author Marek Majchrowski - Initial contribution
 */

public class RfLinkOregonTempHygroMessage extends RfLinkBaseMessage {
    private static final String KEY_TEMPERATURE = "TEMP";
    private static final String KEY_HUMIDITY = "HUM";
    private static final String KEY_HUMIDITY_STATUS = "HSTATUS";
    private static final String KEY_LOW_BATTERY = "BAT";
    private static final List<String> keys = Arrays.asList(KEY_TEMPERATURE, KEY_HUMIDITY, KEY_HUMIDITY_STATUS, KEY_LOW_BATTERY);

    public double temperature = 0;
    public int humidity = 0;
    public String humidity_status = "UNKNOWN";
    public Commands battery_status = Commands.OFF;
        
    public enum Commands {
        OFF("OK", OnOffType.OFF),
        ON("LOW", OnOffType.ON),

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
                    if (command ==c.onOffType) {
                        return c;
                    }
                }
            }
            return null;
        }
    }


    public RfLinkOregonTempHygroMessage() {
    }

    public RfLinkOregonTempHygroMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_OREGONTEMPHYGRO;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);


        if (values.containsKey(KEY_TEMPERATURE)) {
            int temp = Integer.parseInt(values.get(KEY_TEMPERATURE), 16);
            if ((temp & 0x8000) > 0) {  // temperature is an signed int16
                temp = temp & 0x7FFF;
                temp = 0-temp;
            }
            temperature = temp / 10.0d;
        }

        if (values.containsKey(KEY_HUMIDITY)) {
            humidity = Integer.parseInt(values.get(KEY_HUMIDITY), 10);
        }

        if (values.containsKey(KEY_HUMIDITY_STATUS)) {
            switch (Integer.parseInt(values.get(KEY_HUMIDITY_STATUS), 10)) {
                case 0: humidity_status = "NORMAL"; break;
                case 1: humidity_status = "COMFORT"; break;
                case 2: humidity_status = "DRY"; break;
                case 3: humidity_status = "WET"; break;
                default: humidity_status = "UNKNOWN"; break;
            }
        }

        if (values.containsKey(KEY_LOW_BATTERY)) {
            try {
                battery_status = Commands.fromString(values.get(KEY_LOW_BATTERY));
                if (battery_status == null) {
                    throw new RfLinkException("Can't convert " + values.get(KEY_LOW_BATTERY) + " to Switch Command");
                }
            } catch (Exception e) {
                battery_status = Commands.UNKNOWN;
            }
        }
    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public HashMap<String, State> getStates() {

        HashMap<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_TEMPERATURE, new DecimalType(this.temperature));
        map.put(RfLinkBindingConstants.CHANNEL_HUMIDITY, new DecimalType(this.humidity));
        map.put(RfLinkBindingConstants.CHANNEL_HUMIDITY_STATUS, new StringType(this.humidity_status));
        if (this.battery_status.getOnOffType() != null) {
            map.put(RfLinkBindingConstants.CHANNEL_LOW_BATTERY, this.battery_status.getOnOffType());
        }




        return map;
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", temperature = " + temperature;
        str += ", humidity = " + humidity;
        str += ", humidity status = " + humidity_status;
        str += ", low battery status = " + battery_status;

        return str;
    }
    
    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
