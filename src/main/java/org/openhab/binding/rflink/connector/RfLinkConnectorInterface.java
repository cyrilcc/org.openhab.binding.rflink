/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.connector;

import java.io.IOException;

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
     * Procedure for send raw data to RFLink controller.
     *
     * @param data
     *            raw bytes.
     */
    public void sendMessage(byte[] data) throws IOException;

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
