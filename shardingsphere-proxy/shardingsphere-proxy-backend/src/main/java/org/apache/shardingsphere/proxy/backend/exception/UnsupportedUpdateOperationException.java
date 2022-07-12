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

package org.apache.shardingsphere.proxy.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Unsupported update operation exception.
 */
@RequiredArgsConstructor
@Getter
public final class UnsupportedUpdateOperationException extends BackendException {
    
    private static final long serialVersionUID = -5409739222950362541L;
    
    private final String databaseName;
    
    private final String errorMessage;
    
    public UnsupportedUpdateOperationException(final String databaseName) {
        this(databaseName, String.format("The database %s is read-only", databaseName));
    }
}
