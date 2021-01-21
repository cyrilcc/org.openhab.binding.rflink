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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

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
            rain = RfLinkDataParser.parseHexaToUnsignedInt(values.get(KEY_RAIN));
        }
        if (values.containsKey(KEY_RAIN_RATE)) {
            rainRate = RfLinkDataParser.parseHexaToUnsignedInt(values.get(KEY_RAIN));
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
