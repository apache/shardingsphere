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

package org.apache.shardingsphere.mode.manager.cluster.service;

import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.service.MetaDataManagerPersistServiceBuilder;

/**
 * Cluster meta data manager persist service builder.
 */
public final class ClusterMetaDataManagerPersistServiceBuilder implements MetaDataManagerPersistServiceBuilder {
    
    @Override
    public MetaDataManagerPersistService build(final ContextManager contextManager) {
        return new ClusterMetaDataManagerPersistService(contextManager);
    }
    
    @Override
    public Object getType() {
        return "Cluster";
    }
}
