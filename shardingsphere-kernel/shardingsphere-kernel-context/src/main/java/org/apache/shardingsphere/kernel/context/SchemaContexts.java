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

package org.apache.shardingsphere.kernel.context;

import lombok.Getter;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Getter
public final class SchemaContexts implements SchemaContextsAware {
    
    private final Map<String, SchemaContext> schemaContexts = new HashMap<>();
    
    private final ConfigurationProperties props;
    
    private final Authentication authentication;
    
    private final boolean isCircuitBreak;
    
    public SchemaContexts() {
        props = new ConfigurationProperties(new Properties());
        authentication = new Authentication();
        isCircuitBreak = false;
    }
    
    public SchemaContexts(final Map<String, SchemaContext> schemaContexts, final ConfigurationProperties props, final Authentication authentication) {
        this.schemaContexts.putAll(schemaContexts);
        this.props = props;
        this.authentication = authentication;
        isCircuitBreak = false;
    }
    
    public SchemaContexts(final Map<String, SchemaContext> schemaContexts, final ConfigurationProperties props, final Authentication authentication, final boolean isCircuitBreak) {
        this.schemaContexts.putAll(schemaContexts);
        this.props = props;
        this.authentication = authentication;
        this.isCircuitBreak = isCircuitBreak;
    }
    
    @Override
    public SchemaContext getDefaultSchemaContext() {
        return schemaContexts.get(DefaultSchema.LOGIC_NAME);
    }
    
    @Override
    public void close() {
        for (SchemaContext each : schemaContexts.values()) {
            each.getRuntimeContext().getExecutorKernel().close();
        }
    }
}
