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

package org.apache.shardingsphere.mcp.api.resource.descriptor;

import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon;

import java.util.List;
import java.util.Map;

/**
 * MCP resource descriptor.
 */
public interface MCPResourceDescriptor {
    
    /**
     * Get resource name.
     *
     * @return resource name
     */
    String getName();
    
    /**
     * Get resource title.
     *
     * @return resource title
     */
    String getTitle();
    
    /**
     * Get resource description.
     *
     * @return resource description
     */
    String getDescription();
    
    /**
     * Get resource icons.
     *
     * @return resource icons
     */
    List<MCPIcon> getIcons();
    
    /**
     * Get resource MIME type.
     *
     * @return resource MIME type
     */
    String getMimeType();
    
    /**
     * Get resource annotations.
     *
     * @return resource annotations
     */
    MCPAnnotations getAnnotations();
    
    /**
     * Get resource metadata.
     *
     * @return resource metadata
     */
    Map<String, Object> getMeta();
}
