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

package org.apache.shardingsphere.example.cluster.mode.raw.jdbc.config;

import org.apache.shardingsphere.example.cluster.mode.raw.jdbc.config.type.RepositoryType;
import org.apache.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosPropertyKey;

import java.util.Properties;

public final class ClusterModeConfigurationUtil {
    
    private static final String NACOS_CONNECTION_STRING = "localhost:8848";
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String ETCD_CONNECTION_STRING = "http://localhost:2379";
    
    public static ModeConfiguration getRepositoryConfiguration(final boolean overwrite, final ShardingType shardingType, final String repositoryType) {
        Properties props = new Properties();
        String repositoryConnection;
        switch (repositoryType) {
            case RepositoryType.NACOS:
                props.setProperty(NacosPropertyKey.URL.getKey(), "jdbc:mysql://localhost:3306/nacos?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
                props.setProperty(NacosPropertyKey.USERNAME.getKey(), "root");
                props.setProperty(NacosPropertyKey.PASSWORD.getKey(), "");
                props.setProperty(NacosPropertyKey.INIT_SCHEMA.getKey(), "true");
                repositoryConnection = NACOS_CONNECTION_STRING;
                break;
            case RepositoryType.ZOOKEEPER:
                repositoryConnection = ZOOKEEPER_CONNECTION_STRING;
                break;
            case RepositoryType.ETCD:
                repositoryConnection = ETCD_CONNECTION_STRING;
                break;
            default:
                throw new UnsupportedOperationException(repositoryType);
        }
        ClusterPersistRepositoryConfiguration clusterRepositoryConfig;
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                clusterRepositoryConfig = new ClusterPersistRepositoryConfiguration(repositoryType, "governance-sharding-data-source", repositoryConnection, props);
                return new ModeConfiguration("Cluster", clusterRepositoryConfig, overwrite);
            case READWRITE_SPLITTING:
                clusterRepositoryConfig = new ClusterPersistRepositoryConfiguration(repositoryType, "governance-readwrite-splitting-data-source", repositoryConnection, props);
                return new ModeConfiguration("Cluster", clusterRepositoryConfig, overwrite);
            case ENCRYPT:
                clusterRepositoryConfig = new ClusterPersistRepositoryConfiguration(repositoryType, "governance-encrypt-data-source", repositoryConnection, props);
                return new ModeConfiguration("Cluster", clusterRepositoryConfig, overwrite);
            case SHADOW:
                clusterRepositoryConfig = new ClusterPersistRepositoryConfiguration(repositoryType, "governance-shadow-data-source", repositoryConnection, props);
                return new ModeConfiguration("Cluster", clusterRepositoryConfig, overwrite);
            default:
                throw new UnsupportedOperationException(shardingType.toString());
        }
    }
}
