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

package org.apache.shardingsphere.mode.node.path.engine.generator;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Node path variable.
 */
@RequiredArgsConstructor
public final class NodePathVariable {
    
    private static final String PREFIX = "${";
    
    private static final String SUFFIX = "}";
    
    private final String input;
    
    /**
     * Find variable name.
     *
     * @return found variable name
     */
    public Optional<String> findVariableName() {
        return isVariable() ? Optional.of(input.substring(PREFIX.length(), input.length() - SUFFIX.length())) : Optional.empty();
    }
    
    private boolean isVariable() {
        return input.startsWith(PREFIX) && input.endsWith(SUFFIX);
    }
}
