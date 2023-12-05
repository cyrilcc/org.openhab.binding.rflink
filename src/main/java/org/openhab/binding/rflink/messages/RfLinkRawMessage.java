/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

/**
 * RfLink "raw" message - aimed to handle and send "raw" messages (keep alive, RTS setup, etc.)
 *
 * @author cartemere - Initial contribution
 */
public class RfLinkRawMessage extends RfLinkBaseMessage {

    public static RfLinkRawMessage PING = new RfLinkRawMessage("10;PING;");

    public RfLinkRawMessage() {
    }

    public RfLinkRawMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public void encodeMessage(String data) {
        rawMessage = data;
    }

    @Override
    public String buildMessage(String suffix) {
        return rawMessage;
    }
}
