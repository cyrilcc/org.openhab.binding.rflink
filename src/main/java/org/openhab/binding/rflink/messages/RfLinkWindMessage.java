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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * RfLink data class for wind message.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkWindMessage extends RfLinkBaseMessage {

    private static final String KEY_WIND_SPEED = "WINSP";
    private static final String KEY_AVERAGE_WIND_SPEED = "AWINSP";
    private static final String KEY_WIND_DIRECTION = "WINDIR";
    private static final String KEY_W_DIRECTION = "WDIR";
    private static final String KEY_WIND_GUST = "WINGS";
    private static final String KEY_WIND_CHILL = "WINCHL";

    private static final Collection<String> KEYS = Arrays.asList(KEY_WIND_SPEED, KEY_AVERAGE_WIND_SPEED,
            KEY_WIND_DIRECTION, KEY_W_DIRECTION, KEY_WIND_GUST, KEY_WIND_CHILL);

    public int windSpeed = 0;
    public double averageWindSpeed = 0;
    public double windDirection = 0;
    public int windGust = 0;
    public double windChill = 0;

    public RfLinkWindMessage() {

    }

    public RfLinkWindMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_WIND;
    }

    @Override
    public void encodeMessage(String data) {

        super.encodeMessage(data);

        if (values.containsKey(KEY_WIND_SPEED)) {
            windSpeed = Integer.parseInt(values.get(KEY_WIND_SPEED), 16);
        }

        if (values.containsKey(KEY_AVERAGE_WIND_SPEED)) {
            averageWindSpeed = Integer.parseInt(values.get(KEY_AVERAGE_WIND_SPEED), 16) / 10.0f;
        }

        if (values.containsKey(KEY_WIND_DIRECTION)) {
            windDirection = RfLinkDataParser.parseWindDirection(values.get(KEY_WIND_DIRECTION));
        }

        if (values.containsKey(KEY_W_DIRECTION)) {
            windDirection = Integer.parseInt(values.get(KEY_W_DIRECTION), 16);
        }

        if (values.containsKey(KEY_WIND_GUST)) {
            windGust = Integer.parseInt(values.get(KEY_WIND_GUST), 16);
        }

        if (values.containsKey(KEY_WIND_CHILL)) {
            windChill = RfLinkDataParser.parseTemperature(values.get(KEY_WIND_CHILL));
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
        map.put(RfLinkBindingConstants.CHANNEL_AVERAGE_WIND_SPEED, new DecimalType(averageWindSpeed));
        map.put(RfLinkBindingConstants.CHANNEL_GUST, new DecimalType(windGust));
        map.put(RfLinkBindingConstants.CHANNEL_WIND_CHILL, new DecimalType(windChill));
        return map;

    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Wind Speed = " + windSpeed;
        str += ", Avg Wind Speed = " + averageWindSpeed;
        str += ", Wind Direction = " + windDirection;
        str += ", Wind Gust = " + windGust;
        str += ", Wind Chill = " + windChill;
        return str;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
