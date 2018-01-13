/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
