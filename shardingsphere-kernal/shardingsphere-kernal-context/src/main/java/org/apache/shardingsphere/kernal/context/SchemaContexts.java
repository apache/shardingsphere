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

package org.apache.shardingsphere.kernal.context;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class SchemaContexts {
    
    private final Map<String, SchemaContext> schemaContexts = new ConcurrentHashMap<>();
    
    @Setter
    private ConfigurationProperties properties;
    
    @Setter
    private Authentication authentication;
    
    public SchemaContexts() {
        properties = new ConfigurationProperties(new Properties());
        authentication = new Authentication();
    }
    
    public SchemaContexts(final Map<String, SchemaContext> schemaContexts, final ConfigurationProperties properties, final Authentication authentication) {
        this.schemaContexts.putAll(schemaContexts);
        this.properties = properties;
        this.authentication = authentication;
    }
    
    /**
     * Get default schema context.
     * 
     * @return default schema context
     */
    public SchemaContext getDefaultSchemaContext() {
        return schemaContexts.get(DefaultSchema.LOGIC_NAME);
    }
    
    /**
     * Close.
     * 
     */
    public void close() {
        for (SchemaContext each : schemaContexts.values()) {
            each.getRuntimeContext().getExecutorKernel().close();
        }
    }
}
