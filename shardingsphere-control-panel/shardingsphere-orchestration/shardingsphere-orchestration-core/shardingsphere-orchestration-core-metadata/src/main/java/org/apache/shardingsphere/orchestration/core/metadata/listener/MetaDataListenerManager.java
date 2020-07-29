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

package org.apache.shardingsphere.orchestration.core.metadata.listener;

import org.apache.shardingsphere.orchestration.repository.api.OrchestrationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;

import java.util.Collection;

/**
 * Meta data listener manager.
 */
public final class MetaDataListenerManager {
    
    private final MetaDataChangedListener metaDataChangedListener;
    
    public MetaDataListenerManager(final String name, final OrchestrationRepository orchestrationRepository, final Collection<String> schemaNames) {
        metaDataChangedListener = new MetaDataChangedListener(name, orchestrationRepository, schemaNames);
    }
    
    /**
     * Initialize all metadata changed listeners.
     */
    public void initListeners() {
        metaDataChangedListener.watch(ChangedType.UPDATED);
    }
}
