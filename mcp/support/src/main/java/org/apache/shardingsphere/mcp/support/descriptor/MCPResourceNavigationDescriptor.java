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

package org.apache.shardingsphere.mcp.support.descriptor;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * MCP resource navigation descriptor.
 */
@Getter
public final class MCPResourceNavigationDescriptor {
    
    private final String from;
    
    private final String to;
    
    private final List<String> requiredArguments;
    
    private final List<String> carriedArguments;
    
    private final String description;
    
    public MCPResourceNavigationDescriptor(final String from, final String to, final List<String> requiredArguments,
                                           final List<String> carriedArguments, final String description) {
        this.from = from;
        this.to = to;
        this.requiredArguments = null == requiredArguments ? Collections.emptyList() : requiredArguments;
        this.carriedArguments = null == carriedArguments ? Collections.emptyList() : carriedArguments;
        this.description = description;
    }
}
