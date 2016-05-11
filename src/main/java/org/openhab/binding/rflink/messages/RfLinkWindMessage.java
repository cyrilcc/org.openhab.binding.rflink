/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.util.Arrays;
import java.util.List;

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

    private static final List<String> keys = Arrays.asList(KEY_WIND_SPEED, KEY_AVERAGE_WIND_SPEED, KEY_WIND_DIRECTION,
            KEY_W_DIRECTION, KEY_WIND_GUST, KEY_WIND_CHILL);

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
    public List<String> keys() {
        return keys;
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Wind Speed = " + windSpeed;
        str += ", Avg Wind Speed = " + averageWindSpeed;
        str += ", Wind Direction = " + windDirection;
        str += ", Wind Gust = " + windGust;
        str += ", Wind Chill = " + windChill;

        return str;
    }

}
