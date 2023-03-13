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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;

import java.sql.SQLException;

/**
 * Meta data factory.
 */
public final class MetaDataFactory {
    
    /**
     * Create database meta data for governance center.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param internalLoadMetaData internal load meta data
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException sql exception
     */
    public static ShardingSphereDatabase create(final String databaseName, final boolean internalLoadMetaData, final DatabaseConfiguration databaseConfig,
                                                final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        return internalLoadMetaData ? InternalMetaDataFactory.create(databaseName, databaseConfig, props, instanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, instanceContext);
    }
}
