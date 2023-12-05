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
package org.openhab.binding.rflink.connector;

import java.io.IOException;
import java.util.Collection;

/**
 * This interface defines interface to communicate RFLink controller.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public interface RfLinkConnectorInterface {

    /**
     * Procedure for connecting to RFLink controller.
     *
     * @param device
     *            Controller connection parameters (e.g. serial port name or IP
     *            address).
     */
    public void connect(String device, int baudRate) throws Exception;

    /**
     * Procedure for disconnecting to RFLink controller.
     *
     */
    public void disconnect();

    /**
     * Procedure for sending messages data to RFLink controller.
     * Can handle multiple messages at a time (for composite commands)
     *
     * @param data
     *            raw bytes.
     */
    public void sendMessages(Collection<String> messagesData) throws IOException;

    /**
     * Procedure for register event listener.
     *
     * @param listener
     *            Event listener instance to handle events.
     */
    public void addEventListener(RfLinkEventListener listener);

    /**
     * Procedure for remove event listener.
     *
     * @param listener
     *            Event listener instance to remove.
     */
    public void removeEventListener(RfLinkEventListener listener);
}
