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

package org.apache.shardingsphere.governance.core.config.listener.metadata;

import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Collection;

/**
 * Meta data listener.
 */
public final class MetaDataListener {
    
    private final SchemasChangedListener schemasChangedListener;
    
    private final RuleChangedListener ruleChangedListener;
    
    private final DataSourceChangedListener dataSourceChangedListener;
    
    private final SchemaChangedListener schemaChangedListener;
    
    public MetaDataListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        schemasChangedListener = new SchemasChangedListener(configurationRepository, schemaNames);
        ruleChangedListener = new RuleChangedListener(configurationRepository, schemaNames);
        dataSourceChangedListener = new DataSourceChangedListener(configurationRepository, schemaNames);
        schemaChangedListener = new SchemaChangedListener(configurationRepository, schemaNames);
    }
    
    /**
     * watch event.
     */
    public void watch() {
        schemasChangedListener.watch(Type.UPDATED);
        ruleChangedListener.watch(Type.UPDATED);
        dataSourceChangedListener.watch(Type.UPDATED);
        schemaChangedListener.watch(Type.UPDATED);
    }
}
