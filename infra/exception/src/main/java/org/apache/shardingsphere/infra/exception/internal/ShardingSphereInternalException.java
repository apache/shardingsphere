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

package org.apache.shardingsphere.infra.exception.internal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ShardingSphere internal exception.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ShardingSphereInternalException extends Exception {
    
    private static final long serialVersionUID = -8238061892944243621L;
    
    protected ShardingSphereInternalException(final String errorMessage, final Object... args) {
        super(formatMessage(errorMessage, args));
    }
    
    protected ShardingSphereInternalException(final Exception cause) {
        super(cause);
    }
    
    protected ShardingSphereInternalException(final String message, final Exception cause) {
        super(message, cause);
    }
    
    private static String formatMessage(final String reason, final Object[] messageArgs) {
        if (null == reason) {
            return null;
        }
        if (0 == messageArgs.length) {
            return reason;
        }
        return String.format(reason, messageArgs);
    }
}
