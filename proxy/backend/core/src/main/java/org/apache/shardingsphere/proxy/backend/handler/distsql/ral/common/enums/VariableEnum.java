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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums;

import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;

/**
 * Variable enum.
 */
public enum VariableEnum {
    
    CACHED_CONNECTIONS;
    
    /**
     * Returns the variable constant of the specified variable name.
     * 
     * @param variableName variable name
     * @return variable constant
     * @throws UnsupportedVariableException unsupported variable exception
     */
    public static VariableEnum getValueOf(final String variableName) {
        try {
            return valueOf(variableName.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            throw new UnsupportedVariableException(variableName);
        }
    }
}
