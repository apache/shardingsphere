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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;

/**
 * CDC response message generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCResponseGenerator {
    
    /**
     * Succeed response builder.
     *
     * @param requestId request id
     * @return succeed response builder
     */
    public static Builder succeedBuilder(final String requestId) {
        return CDCResponse.newBuilder().setStatus(Status.SUCCEED).setRequestId(requestId);
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
