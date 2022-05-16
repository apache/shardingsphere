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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.loader.DatabaseLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Database Meta data contexts builder.
 */
@Getter
public final class DatabaseMetaDataContextsBuilder {
    
    private final DatabaseConfiguration databaseConfig;
    
    private final Collection<ShardingSphereRule> databaseRules;
    
    private final ShardingSphereDatabase database;
    
    public DatabaseMetaDataContextsBuilder(final String databaseName, final DatabaseType frontendDatabaseType, final DatabaseType backendDatabaseType,
                                           final DatabaseConfiguration databaseConfig, final ConfigurationProperties props) throws SQLException {
        this.databaseConfig = databaseConfig;
        databaseRules = SchemaRulesBuilder.buildRules(databaseName, databaseConfig, props);
        database = DatabaseLoader.load(databaseName, frontendDatabaseType, backendDatabaseType, databaseConfig.getDataSources(), databaseRules, props);
    }
}
