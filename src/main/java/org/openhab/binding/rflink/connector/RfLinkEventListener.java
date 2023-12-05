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

/**
 * This interface defines interface to receive data from RfLink controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RfLinkEventListener {

    /**
     * Procedure for receive raw data from RfLink controller.
     *
     * @param data
     *            Received raw data.
     */
    void packetReceived(String data);

    /**
     * Procedure for receiving information fatal error.
     *
     * @param error
     *            Error occured.
     */
    void errorOccured(String error);
}
