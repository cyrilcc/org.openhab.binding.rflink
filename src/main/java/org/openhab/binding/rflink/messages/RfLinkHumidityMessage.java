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
 * RfLink data class for humidity message.
 *
 * @author Marvyn Zalewski - Initial contribution
 */
public class RfLinkHumidityMessage extends RfLinkBaseMessage {
    private static final String KEY_HUMIDITY = "HUM";
    private static final Collection<String> KEYS = Arrays.asList(KEY_HUMIDITY);

    public double humidity = 0;

    public RfLinkHumidityMessage() {
    }

    public RfLinkHumidityMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_HUMIDITY;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);

        if (values.containsKey(KEY_HUMIDITY)) {
            String val = values.get(KEY_HUMIDITY);
            if (val != null) {
                humidity = Integer.parseInt(val);
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
        map.put(RfLinkBindingConstants.CHANNEL_HUMIDITY, new DecimalType(humidity));

        return map;
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Humidity = " + humidity;

        return str;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
