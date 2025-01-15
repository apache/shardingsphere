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

package org.apache.shardingsphere.mode.metadata.refresher.type.table;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.mode.metadata.refresher.MetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.divided.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTableStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for create table statement.
 */
public final class CreateTableStatementSchemaRefresher implements MetaDataRefresher<CreateTableStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DatabaseType databaseType, final CreateTableStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String tableName = TableRefreshUtils.getTableName(databaseType, sqlStatement.getTable().getTableName().getIdentifier());
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        boolean isSingleTable = TableRefreshUtils.isSingleTable(tableName, database);
        if (isSingleTable) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceNames.iterator().next(), schemaName, tableName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(Collections.singletonList(tableName), database.getProtocolType(), material);
        Optional<ShardingSphereTable> actualTableMetaData = Optional.ofNullable(schemas.get(schemaName)).map(optional -> optional.getTable(tableName));
        Preconditions.checkState(actualTableMetaData.isPresent(), "Load actual table metadata '%s' failed.", tableName);
        metaDataManagerPersistService.createTable(database.getName(), schemaName, actualTableMetaData.get(), logicDataSourceNames.isEmpty() ? null : logicDataSourceNames.iterator().next());
        if (isSingleTable && TableRefreshUtils.isRuleRefreshRequired(ruleMetaData, schemaName, tableName)) {
            metaDataManagerPersistService.alterSingleRuleConfiguration(database.getName(), ruleMetaData.getConfigurations());
        }
    }
    
    @Override
    public Class<CreateTableStatement> getType() {
        return CreateTableStatement.class;
    }
}
