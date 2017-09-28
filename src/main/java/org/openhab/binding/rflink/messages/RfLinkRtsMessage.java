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

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.rflink.RfLinkBindingConstants;

/**
 * RfLink data class for Somfy/RTS message. Dummy? class for item. No inbound messages from RfLink, only outbound.
 *
 * @author John Jore - Initial contribution
 */
public class RfLinkRtsMessage extends RfLinkBaseMessage {
    private static final String KEY_RTS = "RTS";
    private static final List<String> keys = Arrays.asList(KEY_RTS);

    public String switchCode = "";

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
    public String getDeviceId() {
        return super.getDeviceId() + ID_DELIMITER + switchCode;
    }

    @Override
    public String toString() {
        String str = "";
        str += super.toString();
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
        return map;
    }
}
