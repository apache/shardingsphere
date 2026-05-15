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

package org.apache.shardingsphere.mcp.support.descriptor.yaml;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;

/**
 * YAML MCP resource annotations.
 */
@Getter
@Setter
public final class YamlMCPResourceAnnotations {
    
    @Size(min = 1, message = "must not be empty")
    private List<@NotBlank(message = "is required") String> audience = new LinkedList<>();
    
    private double priority;
    
    private boolean priorityPresent;
    
    private String lastModified;
    
    /**
     * Set priority and mark it as explicitly declared.
     *
     * @param priority priority
     */
    public void setPriority(final double priority) {
        this.priority = priority;
        priorityPresent = true;
    }
}
