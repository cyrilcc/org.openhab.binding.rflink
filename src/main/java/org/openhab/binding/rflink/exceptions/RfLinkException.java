/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.exceptions;

/**
 * Exception for RFLink errors.
 *
 * @author Cyril Cauchois - Initial contribution
 */
public class RfLinkException extends Exception {

    private static final long serialVersionUID = 1266931323464544105L;

    public RfLinkException() {
        super();
    }

    public RfLinkException(String message) {
        super(message);
    }

    public RfLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public RfLinkException(Throwable cause) {
        super(cause);
    }

}
