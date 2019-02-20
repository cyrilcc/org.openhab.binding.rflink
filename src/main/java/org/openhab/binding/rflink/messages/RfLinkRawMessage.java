/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
