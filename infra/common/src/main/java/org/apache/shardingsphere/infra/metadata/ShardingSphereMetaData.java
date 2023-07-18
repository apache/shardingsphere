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
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere meta data.
 */
@Getter
public final class ShardingSphereMetaData {
    
    private final Map<String, ShardingSphereDatabase> databases;
    
    private final ShardingSphereResourceMetaData globalResourceMetaData;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final TemporaryConfigurationProperties temporaryProps;
    
    public ShardingSphereMetaData() {
        this(new LinkedHashMap<>(), new ShardingSphereResourceMetaData(Collections.emptyMap()), new ShardingSphereRuleMetaData(Collections.emptyList()),
                new ConfigurationProperties(new Properties()));
    }
    
    public ShardingSphereMetaData(final Map<String, ShardingSphereDatabase> databases, final ShardingSphereResourceMetaData globalResourceMetaData,
                                  final ShardingSphereRuleMetaData globalRuleMetaData, final ConfigurationProperties props) {
        this.databases = new ConcurrentHashMap<>(databases.size(), 1F);
        databases.forEach((key, value) -> this.databases.put(key.toLowerCase(), value));
        this.globalResourceMetaData = globalResourceMetaData;
        this.globalRuleMetaData = globalRuleMetaData;
        this.props = props;
        temporaryProps = new TemporaryConfigurationProperties(props.getProps());
    }
    
    /**
     * Add database.
     *
     * @param databaseName database name
     * @param protocolType protocol database type
     * @param props configuration properties
     */
    public void addDatabase(final String databaseName, final DatabaseType protocolType, final ConfigurationProperties props) {
        ShardingSphereDatabase database = ShardingSphereDatabase.create(databaseName, protocolType, props);
        putDatabase(database);
        globalRuleMetaData.findRules(ResourceHeldRule.class).forEach(each -> each.addResource(database));
    }
    
    /**
     * Judge contains database from meta data or not.
     *
     * @param databaseName database name
     * @return contains database from meta data or not
     */
    public boolean containsDatabase(final String databaseName) {
        return databases.containsKey(databaseName.toLowerCase());
    }
    
    /**
     * Get database.
     *
     * @param databaseName database name
     * @return meta data database
     */
    public ShardingSphereDatabase getDatabase(final String databaseName) {
        return databases.get(databaseName.toLowerCase());
    }
    
    /**
     * Put database.
     *
     * @param database database
     */
    public void putDatabase(final ShardingSphereDatabase database) {
        databases.put(database.getName().toLowerCase(), database);
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public void dropDatabase(final String databaseName) {
        ShardingSphereDatabase toBeRemovedDatabase = databases.remove(databaseName.toLowerCase());
        closeResources(toBeRemovedDatabase);
    }
    
    private void closeResources(final ShardingSphereDatabase database) {
        String databaseName = database.getName();
        globalRuleMetaData.findRules(ResourceHeldRule.class).forEach(each -> each.closeStaleResource(databaseName));
        database.getRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.closeStaleResource(databaseName));
        database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class).ifPresent(StaticDataSourceContainedRule::cleanStorageNodeDataSources);
        Optional.ofNullable(database.getResourceMetaData()).ifPresent(optional -> optional.getDataSources().values().forEach(each -> database.getResourceMetaData().close(each)));
    }
}
