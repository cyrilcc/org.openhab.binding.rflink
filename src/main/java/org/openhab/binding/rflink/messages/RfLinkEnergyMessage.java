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
 * RfLink data class for energy message.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkEnergyMessage extends RfLinkBaseMessage {

    private static float WATTS_TO_AMPS_CONVERSION_FACTOR = 230F;

    private static final String KEY_INSTANT_POWER = "WATT";
    private static final String KEY_TOTAL_POWER = "KWATT";

    private static final List<String> keys = Arrays.asList(KEY_INSTANT_POWER, KEY_TOTAL_POWER);

    public double instantAmps = 0;
    public double totalAmpHours = 0;
    public double instantPower = 0;
    public double totalUsage = 0;

    public RfLinkEnergyMessage() {

    }

    public RfLinkEnergyMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_ENERGY;
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Instant Power = " + instantPower;
        str += ", Total Usage = " + totalUsage;
        str += ", Instant Amps = " + instantAmps;
        str += ", Total Amp Hours = " + totalAmpHours;

        return str;
    }

    @Override
    public void encodeMessage(String data) {

        super.encodeMessage(data);

        // all usage is reported in Watts based on 230V
        if (values.containsKey(KEY_INSTANT_POWER)) {
            instantPower = Integer.parseInt(values.get(KEY_INSTANT_POWER), 16);
            instantAmps = instantPower / WATTS_TO_AMPS_CONVERSION_FACTOR;
        }

        if (values.containsKey(KEY_TOTAL_POWER)) {
            totalUsage = Integer.parseInt(values.get(KEY_TOTAL_POWER), 16);
            totalAmpHours = totalUsage / WATTS_TO_AMPS_CONVERSION_FACTOR;
        }

    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public HashMap<String, State> getStates() {

        HashMap<String, State> map = new HashMap<>();

        map.put(RfLinkBindingConstants.CHANNEL_INSTANT_POWER, new DecimalType(instantPower));
        map.put(RfLinkBindingConstants.CHANNEL_INSTANT_AMPS, new DecimalType(instantAmps));
        map.put(RfLinkBindingConstants.CHANNEL_TOTAL_AMP_HOURS, new DecimalType(totalAmpHours));
        map.put(RfLinkBindingConstants.CHANNEL_TOTAL_USAGE, new DecimalType(totalUsage));

        return map;

    }
    
    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException {
        throw new RfLinkNotImpException("Message handler for " + channelUID + " does not support message transmission");
    }
}
