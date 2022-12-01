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

import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.proto.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.proto.response.CDCResponse.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.proto.response.CDCResponse.Status;

/**
 * CDC response message generator.
 */
public final class CDCResponseGenerator {
    
    /**
     * Response succeed builder.
     *
     * @param requestId request id
     * @return success message
     */
    public static Builder succeedBuilder(final String requestId) {
        Builder builder = CDCResponse.newBuilder();
        builder.setStatus(Status.SUCCEED);
        builder.setRequestId(requestId);
        return builder;
    }
    
    /**
     * Failed response.
     *
     * @param requestId request id
     * @param errorCode error code
     * @param errorMessage error message
     * @return failed response
     */
    public static CDCResponse failed(final String requestId, final CDCResponseErrorCode errorCode, final String errorMessage) {
        Builder builder = CDCResponse.newBuilder();
        builder.setStatus(Status.FAILED);
        builder.setRequestId(requestId);
        builder.setErrorCode(errorCode.getCode());
        builder.setErrorMessage(errorMessage);
        return builder.build();
    }
}
