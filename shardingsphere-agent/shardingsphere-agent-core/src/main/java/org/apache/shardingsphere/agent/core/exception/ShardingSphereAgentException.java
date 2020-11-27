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

package org.apache.shardingsphere.agent.core.exception;

/**
 * Basic exception of ShardingSphere agent.
 */
public class ShardingSphereAgentException extends RuntimeException {
    
    private static final long serialVersionUID = -1343739516839252250L;
    
    /**
     * Constructs an exception with formatted error message and arguments.
     *
     * @param errorMessage formatted error message
     * @param args arguments of error message
     */
    public ShardingSphereAgentException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }
    
    /**
     * Constructs an exception with error message and cause.
     *
     * @param message error message
     * @param cause error cause
     */
    public ShardingSphereAgentException(final String message, final Exception cause) {
        super(message, cause);
    }
    
    /**
     * Constructs an exception with cause.
     *
     * @param cause error cause
     */
    public ShardingSphereAgentException(final Exception cause) {
        super(cause);
    }
}
