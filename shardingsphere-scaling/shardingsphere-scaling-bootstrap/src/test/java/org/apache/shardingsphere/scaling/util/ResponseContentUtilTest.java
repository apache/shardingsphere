/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.scaling.util;

import org.apache.shardingsphere.scaling.web.entity.ResponseCode;
import org.apache.shardingsphere.scaling.web.entity.ResponseContent;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ResponseContentUtilTest {
    
    public static final String ERROR_MESSAGE = "error message.";
    
    @Test
    public void assertHandleBadRequest() {
        ResponseContent<?> responseContent = ResponseContentUtil.handleBadRequest(ERROR_MESSAGE);
        assertThat(responseContent.getErrorMsg(), is(ERROR_MESSAGE));
        assertThat(responseContent.getErrorCode(), is(ResponseCode.BAD_REQUEST));
        assertNull(responseContent.getModel());
    }
    
    @Test
    public void assertHandleException() {
        ResponseContent<?> responseContent = ResponseContentUtil.handleException(ERROR_MESSAGE);
        assertThat(responseContent.getErrorMsg(), is(ERROR_MESSAGE));
        assertThat(responseContent.getErrorCode(), is(ResponseCode.SERVER_ERROR));
        assertNull(responseContent.getModel());
    }
}
