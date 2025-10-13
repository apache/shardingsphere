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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.ResponseCase;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.ServerGreetingResult;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.StreamDataResult;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;

/**
 * CDC response utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCResponseUtils {
    
    /**
     * Succeed response.
     *
     * @param requestId request id
     * @return CDC response
     */
    public static CDCResponse succeed(final String requestId) {
        return succeed(requestId, ResponseCase.RESPONSE_NOT_SET, null);
    }
    
    /**
     * Succeed response.
     *
     * @param requestId request id
     * @param responseCase response case
     * @param response response
     * @return succeed response builder
     * @throws PipelineInvalidParameterException pipeline invalid parameter exception
     */
    public static CDCResponse succeed(final String requestId, final CDCResponse.ResponseCase responseCase, final Message response) {
        Builder result = CDCResponse.newBuilder().setStatus(Status.SUCCEED).setRequestId(requestId);
        switch (responseCase) {
            case SERVER_GREETING_RESULT:
                result.setServerGreetingResult((ServerGreetingResult) response);
                break;
            case DATA_RECORD_RESULT:
                result.setDataRecordResult((DataRecordResult) response);
                break;
            case STREAM_DATA_RESULT:
                result.setStreamDataResult((StreamDataResult) response);
                break;
            case RESPONSE_NOT_SET:
                break;
            default:
                throw new PipelineInvalidParameterException("Unknown response case: `" + responseCase.name() + "`.");
        }
        return result.build();
    }
    
    /**
     * Failed response.
     *
     * @param requestId request id
     * @param errorCode error code
     * @param errorMessage error message
     * @return failed response
     */
    public static CDCResponse failed(final String requestId, final String errorCode, final String errorMessage) {
        return CDCResponse.newBuilder().setStatus(Status.FAILED).setRequestId(requestId).setErrorCode(errorCode).setErrorMessage(errorMessage).build();
    }
}
