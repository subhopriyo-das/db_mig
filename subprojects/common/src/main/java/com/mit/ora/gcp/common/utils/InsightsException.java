/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.common.utils;

public class InsightsException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message - Custom Error Message
     */
    public InsightsException(String message) {
        super(message);
    }

    /**
     * @param cause - Actual Exception/Error
     */
    public InsightsException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message - Custom Error Message
     * @param cause - Actual Exception/Error
     */
    public InsightsException(String message, Throwable cause) {
        super(message, cause);
    }
}
