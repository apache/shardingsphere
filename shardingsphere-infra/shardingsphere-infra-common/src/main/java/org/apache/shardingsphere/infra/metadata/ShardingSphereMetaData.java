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

package org.apache.shardingsphere.infra.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meta data contexts.
 */
@Getter
public final class ShardingSphereMetaData {
    
    private final Map<String, ShardingSphereDatabase> databases;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    public ShardingSphereMetaData() {
        this(new LinkedHashMap<>(), new ShardingSphereRuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    public ShardingSphereMetaData(final Map<String, ShardingSphereDatabase> databases, final ShardingSphereRuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        this.databases = new ConcurrentHashMap<>(databases);
        this.globalRuleMetaData = globalRuleMetaData;
        this.props = props;
    }
    
    /**
     * Add database.
     * 
     * @param databaseName database name
     * @param protocolType protocol database type
     * @throws SQLException SQL exception
     */
    public void addDatabase(final String databaseName, final DatabaseType protocolType) throws SQLException {
        ShardingSphereDatabase database = ShardingSphereDatabase.create(databaseName, protocolType);
        databases.put(databaseName, database);
        globalRuleMetaData.findRules(ResourceHeldRule.class).forEach(each -> each.addResource(database));
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public void dropDatabase(final String databaseName) {
        closeResources(databases.remove(databaseName));
    }
    
    private void closeResources(final ShardingSphereDatabase database) {
        if (null != database.getResource()) {
            database.getResource().getDataSources().values().forEach(each -> database.getResource().close(each));
        }
        String databaseName = database.getName();
        globalRuleMetaData.findRules(ResourceHeldRule.class).forEach(each -> each.closeStaleResource(databaseName));
        database.getRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.closeStaleResource(databaseName));
    }
}
