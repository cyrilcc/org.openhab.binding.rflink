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
import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;

/**
 * RfLink data class for rain message.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkRainMessage extends RfLinkBaseMessage {

    private static final String KEY_RAIN = "RAIN";
    private static final String KEY_RAIN_RATE = "RAINRATE";

    private static final List<String> keys = Arrays.asList(KEY_RAIN, KEY_RAIN_RATE);

    public double rain = 0;
    public double rainRate = 0;

    public RfLinkRainMessage() {

    }

    public RfLinkRainMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public void encodeMessage(String data) {

        super.encodeMessage(data);

        if (values.containsKey(KEY_RAIN)) {
            rain = Integer.parseInt(values.get(KEY_RAIN), 16) / 10.0f;
        }

        if (values.containsKey(KEY_RAIN_RATE)) {
            rainRate = Integer.parseInt(values.get(KEY_RAIN_RATE), 16) / 10.0f;
        }

    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public HashMap<String, State> getStates() {

        HashMap<String, State> map = new HashMap<>();

        map.put(RfLinkBindingConstants.CHANNEL_RAIN_TOTAL, new DecimalType(rain));
        map.put(RfLinkBindingConstants.CHANNEL_RAIN_RATE, new DecimalType(rainRate));

        return map;

    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Rain Total = " + rain;
        str += ", Rain Rate = " + rainRate;

        return str;
    }

}
