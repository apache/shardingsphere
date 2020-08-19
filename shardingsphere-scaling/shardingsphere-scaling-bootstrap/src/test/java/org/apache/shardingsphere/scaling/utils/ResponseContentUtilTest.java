package org.apache.shardingsphere.scaling.utils;

import org.apache.shardingsphere.scaling.web.entity.ResponseCode;
import org.apache.shardingsphere.scaling.web.entity.ResponseContent;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ResponseContentUtilTest {
    
    public static final String ERROR_MESSAGE = "error message.";
    
    @Test
    public void assertHandleBadRequest() {
        ResponseContent responseContent = ResponseContentUtil.handleBadRequest(ERROR_MESSAGE);
        assertThat(responseContent.getErrorMsg(), is(ERROR_MESSAGE));
        assertThat(responseContent.getErrorCode(), is(ResponseCode.BAD_REQUEST));
        assertNull(responseContent.getModel());
    }
    
    @Test
    public void assertHandleException() {
        ResponseContent responseContent = ResponseContentUtil.handleException(ERROR_MESSAGE);
        assertThat(responseContent.getErrorCode(), is(ResponseCode.SERVER_ERROR));
    }
}
