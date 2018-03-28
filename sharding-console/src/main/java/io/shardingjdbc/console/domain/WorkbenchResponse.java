package io.shardingjdbc.console.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Return response object.
 * 
 * @author panjuan
 */
@AllArgsConstructor
@Getter
@Setter
public final class WorkbenchResponse {
    
    private int status;
    
    private String message;
    
    private Object data;

    /**
     * Generate response object.
     * @param responseCode  response code. 
     * @param responseMsg response message.
     */
    public WorkbenchResponse(final int responseCode, final String responseMsg) {
        status = responseCode;
        message = responseMsg;
    }
}
