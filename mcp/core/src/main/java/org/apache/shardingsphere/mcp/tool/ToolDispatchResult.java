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

package org.apache.shardingsphere.mcp.tool;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.resource.MetadataObject;
import org.apache.shardingsphere.mcp.resource.ResourceLoadResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Dispatch result for one metadata tool request.
 */
@Getter
public final class ToolDispatchResult {
    
    private final List<MetadataObject> metadataObjects;
    
    private final String nextPageToken;
    
    @Getter(AccessLevel.NONE)
    private final boolean errorCodePresent;
    
    @Getter(AccessLevel.NONE)
    private final ErrorCode errorCode;
    
    private final String message;
    
    private ToolDispatchResult(final Collection<MetadataObject> metadataObjects, final String nextPageToken,
                               final boolean errorCodePresent, final ErrorCode errorCode, final String message) {
        this.metadataObjects = Collections.unmodifiableList(new ArrayList<>(metadataObjects));
        this.nextPageToken = Objects.requireNonNull(nextPageToken, "nextPageToken cannot be null");
        this.errorCodePresent = errorCodePresent;
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
    }
    
    /**
     * Determine whether the tool request finished successfully.
     *
     * @return {@code true} when no error is attached
     */
    public boolean isSuccessful() {
        return !errorCodePresent;
    }
    
    static ToolDispatchResult success(final Collection<MetadataObject> metadataObjects, final String nextPageToken) {
        return new ToolDispatchResult(metadataObjects, nextPageToken, false, ErrorCode.INVALID_REQUEST, "");
    }
    
    static ToolDispatchResult error(final ErrorCode errorCode, final String message) {
        return new ToolDispatchResult(Collections.emptyList(), "", true, Objects.requireNonNull(errorCode, "errorCode cannot be null"), message);
    }
    
    static ToolDispatchResult fromResourceLoadResult(final ResourceLoadResult loadResult) {
        return loadResult.getErrorCode().isPresent()
                ? error(loadResult.getErrorCode().get(), loadResult.getMessage())
                : success(loadResult.getMetadataObjects(), "");
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
