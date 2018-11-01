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
import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * RfLink data class for Somfy/RTS message.
 *
 * @author John Jore - Initial contribution
 * @author Arjan Mels - Added reception and debugged sending
 */
public class RfLinkRtsMessage extends RfLinkBaseMessage {
    private static final String KEY_RTS = "RTS";
    private static final List<String> keys = Arrays.asList(KEY_RTS);

    public Command command = null;
    public UpDownType state = null;

    public RfLinkRtsMessage() {
    }

    public RfLinkRtsMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_RTS;
    }

    @Override
    public String toString() {
        String str = "";
        str += super.toString();
        str += ", State = " + state;
        str += ", Command = " + command;
        return str;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);
    }

    @Override
    public List<String> keys() {
        return keys;
    }

    @Override
    public HashMap<String, State> getStates() {
        HashMap<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_SHUTTER, state);
        return map;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);
        command = triggeredCommand;
        if (triggeredCommand.toFullString().equals(UpDownType.UP.toFullString())) {
            state = UpDownType.UP;
        } else if (triggeredCommand.toFullString().equals(UpDownType.DOWN.toFullString())) {
            state = UpDownType.DOWN;
        }
    }

    @Override
    public String decodeMessageAsString(String suffix) {
        return super.decodeMessageAsString(this.command.toString());
    }
}
