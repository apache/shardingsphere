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

package org.apache.shardingsphere.infra.url.etcd;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereModeConfigurationURLLoader;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.Properties;

/**
 * Etcd mode configuration URL loader.
 */
public final class EtcdModeConfigurationURLLoader implements ShardingSphereModeConfigurationURLLoader {
    
    @Override
    public ModeConfiguration load(final String serverLists, final Properties queryProps) {
        ShardingSpherePreconditions.checkState(queryProps.containsKey("namespace"), () -> new RuntimeException("Missing required property 'namespace' for ETCD URL loader."));
        return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("etcd", queryProps.getProperty("namespace"), serverLists, queryProps));
    }
    
    @Override
    public String getType() {
        return "etcd:";
    }
}
