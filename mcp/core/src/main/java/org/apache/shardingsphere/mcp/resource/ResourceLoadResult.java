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

package org.apache.shardingsphere.mcp.resource;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Loaded resource result for one request.
 */
@Getter
public final class ResourceLoadResult {
    
    private final List<MetadataObject> metadataObjects;
    
    @Getter(AccessLevel.NONE)
    private final boolean errorCodePresent;
    
    @Getter(AccessLevel.NONE)
    private final ErrorCode errorCode;
    
    private final String message;
    
    private ResourceLoadResult(final List<MetadataObject> metadataObjects, final boolean errorCodePresent, final ErrorCode errorCode, final String message) {
        this.metadataObjects = metadataObjects;
        this.errorCodePresent = errorCodePresent;
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
    }
    
    /**
     * Determine whether the load finished successfully.
     *
     * @return {@code true} when no error is attached
     */
    public boolean isSuccessful() {
        return !errorCodePresent;
    }
    
    /**
     * Create a successful resource load result.
     *
     * @param metadataObjects loaded metadata objects
     * @return successful resource load result
     */
    public static ResourceLoadResult success(final List<MetadataObject> metadataObjects) {
        return new ResourceLoadResult(metadataObjects, false, ErrorCode.INVALID_REQUEST, "");
    }
    
    /**
     * Create an error resource load result.
     *
     * @param errorCode unified error code
     * @param message error message
     * @return failed resource load result
     */
    public static ResourceLoadResult error(final ErrorCode errorCode, final String message) {
        return new ResourceLoadResult(Collections.emptyList(), true, errorCode, message);
    }
    
    /**
     * Get the error code when one exists.
     *
     * @return optional error code
     */
    public Optional<ErrorCode> getErrorCode() {
        return errorCodePresent ? Optional.of(errorCode) : Optional.empty();
    }
}
