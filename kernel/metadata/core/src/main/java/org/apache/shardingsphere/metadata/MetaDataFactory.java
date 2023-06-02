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

package org.apache.shardingsphere.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Map;

/**
 * Meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataFactory {
    
    /**
     * Create database meta data for governance center.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param persistService meta data persist service
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException sql exception
     */
    public static ShardingSphereDatabase create(final String databaseName, final boolean internalLoadMetaData, final MetaDataBasedPersistService persistService,
                                                final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        return internalLoadMetaData ? InternalMetaDataFactory.create(databaseName, persistService, databaseConfig, props, instanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, instanceContext);
    }
    
    /**
     * Create database meta data for governance center.
     *
     * @param internalLoadMetaData internal load meta data
     * @param persistService meta data persist service
     * @param databaseConfigMap database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException sql exception
     */
    public static Map<String, ShardingSphereDatabase> create(final boolean internalLoadMetaData, final MetaDataPersistService persistService,
                                                             final Map<String, DatabaseConfiguration> databaseConfigMap, final ConfigurationProperties props,
                                                             final InstanceContext instanceContext) throws SQLException {
        return internalLoadMetaData ? InternalMetaDataFactory.create(persistService, databaseConfigMap, props, instanceContext)
                : ExternalMetaDataFactory.create(databaseConfigMap, props, instanceContext);
    }
}
