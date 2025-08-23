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

package org.apache.shardingsphere.data.pipeline.cdc.generator;

import com.google.protobuf.Message;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CDCResponseUtilsTest {
    
    @Test
    void assertSucceedWhenResponseNotSet() {
        CDCResponse actualResponse = CDCResponseUtils.succeed("request_id_1");
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getRequestId(), is("request_id_1"));
    }
    
    @Test
    void assertSucceedWhenResponseCaseServerGreetingResult() {
        Message msg = ServerGreetingResult.newBuilder().build();
        CDCResponse actualResponse = CDCResponseUtils.succeed("request_id_1", CDCResponse.ResponseCase.SERVER_GREETING_RESULT, msg);
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getRequestId(), is("request_id_1"));
        assertNotNull(actualResponse.getServerGreetingResult());
    }
    
    @Test
    void assertSucceedWhenResponseCaseDataRecordResult() {
        Message msg = DataRecordResult.newBuilder().build();
        CDCResponse actualResponse = CDCResponseUtils.succeed("request_id_1", CDCResponse.ResponseCase.DATA_RECORD_RESULT, msg);
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getRequestId(), is("request_id_1"));
        assertNotNull(actualResponse.getDataRecordResult());
    }
    
    @Test
    void assertSucceedWhenResponseCaseStreamDataResult() {
        Message msg = StreamDataResult.newBuilder().build();
        CDCResponse actualResponse = CDCResponseUtils.succeed("request_id_1", CDCResponse.ResponseCase.STREAM_DATA_RESULT, msg);
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getRequestId(), is("request_id_1"));
        assertNotNull(actualResponse.getStreamDataResult());
    }
    
    @Test
    void assertFailed() {
        CDCResponse actualResponse = CDCResponseUtils.failed("request_id_1", XOpenSQLState.GENERAL_ERROR.getValue(), "Error");
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.FAILED));
        assertThat(actualResponse.getRequestId(), is("request_id_1"));
        assertThat(actualResponse.getErrorCode(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actualResponse.getErrorMessage(), is("Error"));
    }
}
