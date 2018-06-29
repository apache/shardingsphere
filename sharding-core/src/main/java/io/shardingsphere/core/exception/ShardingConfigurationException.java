/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.exception;

/**
 * Sharding rule exception.
 *
 * @author zhangliang
 */
public final class ShardingConfigurationException extends RuntimeException {
    
    private static final long serialVersionUID = -1360264079938958332L;
    
    /**
     * Constructs an exception with formatted error message and arguments. 
     *
     * @param errorMessage formatted error message
     * @param args arguments of error message
     */
    public ShardingConfigurationException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }
    
    /**
     * Constructs an exception with cause exception. 
     *
     * @param cause cause exception
     */
    public ShardingConfigurationException(final Exception cause) {
        super(cause);
    }
}
