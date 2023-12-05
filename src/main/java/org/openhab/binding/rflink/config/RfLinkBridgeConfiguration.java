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
package org.openhab.binding.rflink.config;

import org.openhab.binding.rflink.RfLinkBindingConstants;

/**
 * Configuration class for {@link RflinkBinding} device.
 *
 * @author Cyril Cauchois - Initial contribution
 * @author Marvyn Zalewski - added disableDiscovery Configuration
 */
public class RfLinkBridgeConfiguration {

    // Configuration for discovered bridge devices
    public String bridgeId;

    // Serial port for manual configuration
    public String serialPort;

    // Serial port baud rate
    public int baudRate = RfLinkBindingConstants.BAUD_RATE_DEFAULT;

    // keepAlive ping period
    public int keepAlivePeriod = 0;

    // Prevent unknown devices from being added to the inbox
    public boolean disableDiscovery;
}
