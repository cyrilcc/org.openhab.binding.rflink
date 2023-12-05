/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * RfLink data class for WH1080 weather station message.
 *
 * @author Marek Majchrowski - Initial contribution
 */
public class RfLinkWH1080WeatherStationMessage extends RfLinkBaseMessage {
    private static final String KEY_TEMPERATURE = "TEMP";
    private static final String KEY_HUMIDITY = "HUM";
    private static final String KEY_BATTERY = "BAT";
    private static final String KEY_RAIN = "RAIN";
    private static final String KEY_WIND_SPEED = "WINSP";
    private static final String KEY_WIND_DIRECTION = "WINDIR";
    private static final String KEY_WIND_GUST = "WINGS";

    private static final Collection<String> KEYS = Arrays.asList(KEY_TEMPERATURE, KEY_HUMIDITY, KEY_RAIN, KEY_BATTERY,
            KEY_WIND_SPEED, KEY_WIND_DIRECTION, KEY_WIND_GUST);

    public double temperature = 0;
    public int humidity = 0;
    public RfLinkWH1080WeatherStationMessage.Commands battery_status = RfLinkWH1080WeatherStationMessage.Commands.OFF;
    public int windSpeed = 0;
    public double windDirection = 0;
    public int windGust = 0;
    public double rain = 0;

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

        public static RfLinkWH1080WeatherStationMessage.Commands fromString(String text) {
            if (text != null) {
                for (RfLinkWH1080WeatherStationMessage.Commands c : RfLinkWH1080WeatherStationMessage.Commands
                        .values()) {
                    if (text.equalsIgnoreCase(c.command)) {
                        return c;
                    }
                }
            }
            return null;
        }

        public static RfLinkWH1080WeatherStationMessage.Commands fromCommand(Command command) {
            if (command != null) {
                for (RfLinkWH1080WeatherStationMessage.Commands c : RfLinkWH1080WeatherStationMessage.Commands
                        .values()) {
                    if (command == c.onOffType) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

    public RfLinkWH1080WeatherStationMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_WH1080WEATHERSTATION;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);
        if (values.containsKey(KEY_TEMPERATURE)) {
            temperature = RfLinkDataParser.parseHexaToSignedDecimal(values.get(KEY_TEMPERATURE));
        }

        if (values.containsKey(KEY_HUMIDITY)) {
            humidity = RfLinkDataParser.parseToInt(values.get(KEY_HUMIDITY));
        }

        if (values.containsKey(KEY_RAIN)) {
            rain = RfLinkDataParser.parseHexaToUnsignedInt(values.get(KEY_RAIN));
        }

        if (values.containsKey(KEY_WIND_SPEED)) {
            // should be DECIMAL
            windSpeed = RfLinkDataParser.parseHexaToUnsignedInt(values.get(KEY_WIND_SPEED));
        }

        if (values.containsKey(KEY_WIND_DIRECTION)) {
            windDirection = RfLinkDataParser.parseIntTo360Direction(values.get(KEY_WIND_DIRECTION));
        }

        if (values.containsKey(KEY_WIND_GUST)) {
            // sould be DECIMAL
            windGust = RfLinkDataParser.parseHexaToUnsignedInt(values.get(KEY_WIND_GUST));
        }

        if (values.containsKey(KEY_BATTERY)) {
            try {
                battery_status = Commands.fromString(values.get(KEY_BATTERY));
                if (battery_status == null) {
                    throw new RfLinkException("Can't convert " + values.get(KEY_BATTERY) + " to Switch Command");
                }
            } catch (Exception e) {
                battery_status = Commands.UNKNOWN;
            }
        }
    }

    @Override
    public Collection<String> keys() {
        return KEYS;
    }

    @Override
    public Map<String, State> getStates() {
        Map<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_WIND_SPEED, new DecimalType(windSpeed));
        map.put(RfLinkBindingConstants.CHANNEL_WIND_DIRECTION, new DecimalType(windDirection));
        map.put(RfLinkBindingConstants.CHANNEL_GUST, new DecimalType(windGust));
        map.put(RfLinkBindingConstants.CHANNEL_TEMPERATURE, new DecimalType(this.temperature));
        map.put(RfLinkBindingConstants.CHANNEL_HUMIDITY, new DecimalType(this.humidity));
        map.put(RfLinkBindingConstants.CHANNEL_RAIN_TOTAL, new DecimalType(rain));
        if (this.battery_status.getOnOffType() != null) {
            map.put(RfLinkBindingConstants.CHANNEL_LOW_BATTERY, this.battery_status.getOnOffType());
        }
        return map;
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", temperature = " + temperature;
        str += ", humidity = " + humidity;
        str += ", Rain Total = " + rain;
        str += ", Wind Speed = " + windSpeed;
        str += ", Wind Direction = " + windDirection;
        str += ", Wind Gust = " + windGust;
        str += ", low battery status = " + battery_status;
        return str;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
