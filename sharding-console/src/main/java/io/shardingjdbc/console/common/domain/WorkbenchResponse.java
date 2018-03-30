package io.shardingjdbc.console.common.domain;

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
    
    private String message;
    
    private Object data;
    
    /**
     * Response object.
     *
     * @param responseData response data.
     */
    public WorkbenchResponse(final Object responseData) {
        message = "";
        data = responseData;
    }
    
    public WorkbenchResponse(final String msg) {
        message = msg;
        data = null;
    }
}
