/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.util.Collection;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * This interface defines interface which every message class should implement.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public interface RfLinkMessage {

    /**
     * Procedure for encode raw data.
     *
     * @param data
     *                 Raw data.
     */
    void encodeMessage(String data);

    /**
     * Procedure to decode back into serial message
     *
     * @param additional data to be appended to the message.
     * @return bytes to be send over serial.
     */
    byte[] decodeMessage(String suffix);

    /**
     * procedure to 'decode' a RfLinkMessage into a String for
     * streaming through the gateway.
     *
     * @param suffix
     * @return
     */
    public String decodeMessageAsString(String suffix);

    /**
     * Procedure to get device id.
     *
     * @return device Id.
     */
    String getDeviceId() throws RfLinkException;

    /**
     * Procedure to get device name.
     *
     * @return device Name.
     */
    String getDeviceName();

    /**
     * Procedure to thingType linked to message.
     *
     * @return Thing type.
     */
    ThingTypeUID getThingType();

    /**
     * Get all the value names that concerns this message
     *
     * @return
     */
    Collection<String> keys();

    /**
     * Get all the values in form of smarthome states
     *
     * @return
     */
    Map<String, State> getStates();

    /**
     * Initializes message to be transmitted
     *
     * @return
     * @throws RfLinkException
     */
    void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command command)
            throws RfLinkNotImpException, RfLinkException;
}
