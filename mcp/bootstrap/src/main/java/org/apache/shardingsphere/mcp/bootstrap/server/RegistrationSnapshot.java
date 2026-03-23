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

package org.apache.shardingsphere.mcp.bootstrap.server;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable registration snapshot.
 */
@Getter
public final class RegistrationSnapshot {
    
    private final Set<String> resources;
    
    private final Set<String> tools;
    
    private final boolean running;
    
    /**
     * Construct a registration snapshot.
     *
     * @param resources registered resources
     * @param tools registered tools
     * @param running runtime state
     */
    public RegistrationSnapshot(final Set<String> resources, final Set<String> tools, final boolean running) {
        this.resources = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(resources, "resources cannot be null")));
        this.tools = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(tools, "tools cannot be null")));
        this.running = running;
    }
}
