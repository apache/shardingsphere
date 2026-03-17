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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.schema;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

@Getter
final class SchemaMetaDataManagerPersistServiceFixture implements MetaDataManagerPersistService {
    
    private String createdSchemaName;
    
    private String renamedSchemaName;
    
    private String sourceSchemaName;
    
    private Collection<String> droppedSchemaNames = new LinkedList<>();
    
    @Override
    public void createDatabase(final String databaseName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void dropDatabase(final ShardingSphereDatabase database) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void createSchema(final ShardingSphereDatabase database, final String schemaName) {
        createdSchemaName = schemaName;
    }
    
    @Override
    public void renameSchema(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName) {
        sourceSchemaName = schemaName;
        renamedSchemaName = renameSchemaName;
    }
    
    @Override
    public void dropSchema(final ShardingSphereDatabase database, final Collection<String> schemaNames) {
        droppedSchemaNames = new LinkedList<>(schemaNames);
    }
    
    @Override
    public void createTable(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereTable table) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void dropTables(final ShardingSphereDatabase database, final String schemaName, final Collection<String> tableNames) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterTables(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereTable> alteredTables) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterViews(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereView> alteredViews) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void dropViews(final ShardingSphereDatabase database, final String schemaName, final Collection<String> droppedViews) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterStorageUnits(final ShardingSphereDatabase database, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void unregisterStorageUnits(final ShardingSphereDatabase database, final Collection<String> toBeDroppedStorageUnitNames) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterSingleRuleConfiguration(final ShardingSphereDatabase database, final RuleMetaData ruleMetaData) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeAlteredRuleConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeRuleConfigurationItem(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleItemConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleConfig, final String ruleType) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration globalRuleConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void alterProperties(final Properties props) {
        throw new UnsupportedOperationException();
    }
}
