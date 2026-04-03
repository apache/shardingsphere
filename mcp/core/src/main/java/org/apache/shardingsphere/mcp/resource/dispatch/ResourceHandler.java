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

package org.apache.shardingsphere.mcp.resource.dispatch;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mcp.resource.ResourceHandlerContext;
import org.apache.shardingsphere.mcp.resource.ResourceHandlerResult;
import org.apache.shardingsphere.mcp.uri.UriTemplateMatch;

/**
 * Handler for one MCP resource URI template.
 */
@SingletonSPI
public interface ResourceHandler extends ShardingSphereSPI {
    
    /**
     * Get URI template.
     *
     * @return URI template
     */
    String getUriTemplate();
    
    /**
     * Handle one matched resource URI.
     *
     * @param resourceHandlerContext resource handler context
     * @param uriTemplateMatch matched uri template
     * @return resource handler result
     */
    ResourceHandlerResult handle(ResourceHandlerContext resourceHandlerContext, UriTemplateMatch uriTemplateMatch);
}
