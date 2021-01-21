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

import java.util.Collection;
import java.util.Map;

import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

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
     *            Raw data.
     */
    void encodeMessage(String data);

    /**
     * Procedure generate message[s] to send to the bridge
     *
     * @return Collection of String messages to be send over serial. Several elements in case of composite command
     */
    public Collection<String> buildMessages();

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
