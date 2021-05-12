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

package org.apache.shardingsphere.governance.core.registry.listener.metadata;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.GovernanceRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

import java.util.Collection;

/**
 * Meta data listener.
 */
public final class MetaDataListener {
    
    private final MetaDataChangedListener metaDataChangedListener;
    
    private volatile RuleChangedListener ruleChangedListener;
    
    private volatile DataSourceChangedListener dataSourceChangedListener;
    
    private volatile SchemaChangedListener schemaChangedListener;
    
    private final GovernanceRepository governanceRepository;
    
    public MetaDataListener(final GovernanceRepository governanceRepository, final Collection<String> schemaNames) {
        this.governanceRepository = governanceRepository;
        metaDataChangedListener = new MetaDataChangedListener(governanceRepository, schemaNames);
        ruleChangedListener = new RuleChangedListener(governanceRepository, schemaNames);
        dataSourceChangedListener = new DataSourceChangedListener(governanceRepository, schemaNames);
        schemaChangedListener = new SchemaChangedListener(governanceRepository, schemaNames);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * watch event.
     */
    public void watch() {
        metaDataChangedListener.watch(Type.UPDATED, Type.ADDED);
        ruleChangedListener.watch(Type.UPDATED);
        dataSourceChangedListener.watch(Type.UPDATED);
        schemaChangedListener.watch(Type.UPDATED);
    }
    
    /**
     * Renew listeners.
     * 
     * @param event meta data changed event.
     */
    @Subscribe
    public void renew(final MetaDataChangedEvent event) {
        ruleChangedListener = new RuleChangedListener(governanceRepository, event.getSchemaNames());
        dataSourceChangedListener = new DataSourceChangedListener(governanceRepository, event.getSchemaNames());
        schemaChangedListener = new SchemaChangedListener(governanceRepository, event.getSchemaNames());
        ruleChangedListener.watch(Type.UPDATED);
        dataSourceChangedListener.watch(Type.UPDATED);
        schemaChangedListener.watch(Type.UPDATED);
    }
}
