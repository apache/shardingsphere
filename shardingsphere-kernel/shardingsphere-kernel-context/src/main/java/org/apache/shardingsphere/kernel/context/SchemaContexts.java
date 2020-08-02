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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Schema contexts.
 */
@RequiredArgsConstructor
@Getter
public final class SchemaContexts implements SchemaContextsAware {
    
    private final Map<String, SchemaContext> schemaContexts;
    
    private final Authentication authentication;
    
    private final ConfigurationProperties props;
    
    private final boolean isCircuitBreak;
    
    public SchemaContexts() {
        this(Collections.emptyMap(), new Authentication(), new ConfigurationProperties(new Properties()), false);
    }
    
    public SchemaContexts(final Map<String, SchemaContext> schemaContexts, final Authentication authentication, final ConfigurationProperties props) {
        this(schemaContexts, authentication, props, false);
    }
    
    /**
     * Get rules.
     *
     * @param ruleType rule type
     * @param <T> type of rule
     * @return rules
     */
    public <T extends ShardingSphereRule> Map<String, Collection<T>> getRules(final Class<T> ruleType) {
        return schemaContexts.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> getRules(entry.getValue(), ruleType), (key, value) -> value, LinkedHashMap::new));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ShardingSphereRule> Collection<T> getRules(final SchemaContext schemaContext, final Class<T> ruleType) {
        return schemaContext.getSchema().getRules().stream().filter(each -> ruleType == each.getClass()).map(each -> (T) each).collect(Collectors.toList());
    }
    
    @Override
    public SchemaContext getDefaultSchemaContext() {
        return schemaContexts.get(DefaultSchema.LOGIC_NAME);
    }
    
    @Override
    public void close() {
        schemaContexts.values().forEach(each -> each.getRuntimeContext().getExecutorKernel().close());
    }
}
