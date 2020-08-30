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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute;

import lombok.Getter;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;

@Getter
public final class GovernanceSchemaContextsFixture implements SchemaContexts {
    
    @Override
    public DatabaseType getDatabaseType() {
        return new MySQLDatabaseType();
    }
    
    @Override
    public Map<String, SchemaContext> getSchemaContexts() {
        return Collections.singletonMap("schema", mock(SchemaContext.class));
    }
    
    @Override
    public SchemaContext getDefaultSchemaContext() {
        return mock(SchemaContext.class);
    }
    
    @Override
    public Authentication getAuthentication() {
        return new Authentication();
    }
    
    @Override
    public ConfigurationProperties getProps() {
        return new ConfigurationProperties(new Properties());
    }
    
    @Override
    public boolean isCircuitBreak() {
        return false;
    }
    
    @Override
    public void close() {
    }
}
