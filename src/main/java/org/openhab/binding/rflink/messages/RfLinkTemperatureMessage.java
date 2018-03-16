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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * RfLink data class for temperature message.
 *
 * @author John Jore - Initial contribution
 */

public class RfLinkTemperatureMessage extends RfLinkBaseMessage {
    private static final String KEY_TEMPERATURE = "TEMP";
    private static final List<String> keys = Arrays.asList(KEY_TEMPERATURE);

    public double temperature = 0;

    public RfLinkTemperatureMessage() {
    }

    public RfLinkTemperatureMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_TEMPERATURE;
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
    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public HashMap<String, State> getStates() {

        HashMap<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_TEMPERATURE, new DecimalType(temperature));

        return map;
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Temperature = " + temperature;

        return str;
    }
    
    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
