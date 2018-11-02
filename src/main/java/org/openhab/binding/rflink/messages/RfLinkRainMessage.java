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
import java.util.List;
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
 * RfLink data class for rain message.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkRainMessage extends RfLinkBaseMessage {

    private static final String KEY_RAIN = "RAIN";
    private static final String KEY_RAIN_RATE = "RAINRATE";

    private static final List<String> KEYS = Arrays.asList(KEY_RAIN, KEY_RAIN_RATE);

    public double rain = 0;
    public double rainRate = 0;

    public RfLinkRainMessage() {

    }

    public RfLinkRainMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_RAIN;
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
    public Collection<String> keys() {
        return KEYS;
    }

    @Override
    public Map<String, State> getStates() {
        Map<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_RAIN_TOTAL, new DecimalType(rain));
        map.put(RfLinkBindingConstants.CHANNEL_RAIN_RATE, new DecimalType(rainRate));
        return map;
    }

    @Override
    public String toString() {
        String str = super.toString();
        str += ", Rain Total = " + rain;
        str += ", Rain Rate = " + rainRate;
        return str;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
