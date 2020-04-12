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

package org.apache.shardingsphere.ui.common.exception;

import lombok.Getter;

/**
 * ShardingSphere UI system exception.
 */
@Getter
public final class ShardingSphereUIException extends RuntimeException {
    
    public static final int INVALID_PARAM = 400;
    
    public static final int NO_RIGHT = 403;
    
    public static final int SERVER_ERROR = 500;
    
    private final int errorCode;
    
    private final String errorMessage;
    
    public ShardingSphereUIException(final int errorCode, final String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
}
