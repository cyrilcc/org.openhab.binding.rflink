/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.config;

/**
 * Configuration class for {@link RflinkBinding} device.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkBridgeConfiguration {

    // Configuration for discovered bridge devices
    public String bridgeId;

    // Serial port for manual configuration
    public String serialPort;

    // Serial port baud rate
    public int baudRate;
}