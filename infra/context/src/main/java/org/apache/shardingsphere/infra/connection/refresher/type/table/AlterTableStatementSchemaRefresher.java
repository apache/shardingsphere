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

package org.apache.shardingsphere.infra.connection.refresher.type.table;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.connection.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for alter table statement.
 */
public final class AlterTableStatementSchemaRefresher implements MetaDataRefresher<AlterTableStatement> {
    
    @Override
    public void refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DatabaseType databaseType, final AlterTableStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String tableName = TableRefreshUtils.getTableName(databaseType, sqlStatement.getTable().getTableName().getIdentifier());
        AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), schemaName, logicDataSourceNames);
        if (sqlStatement.getRenameTable().isPresent()) {
            String renameTable = sqlStatement.getRenameTable().get().getTableName().getIdentifier().getValue();
            alterSchemaMetaDataPOJO.getAlteredTables().add(getTable(database, logicDataSourceNames, schemaName, renameTable, props));
            alterSchemaMetaDataPOJO.getDroppedTables().add(tableName);
        } else {
            alterSchemaMetaDataPOJO.getAlteredTables().add(getTable(database, logicDataSourceNames, schemaName, tableName, props));
        }
        modeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
    }
    
    private ShardingSphereTable getTable(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames, final String schemaName,
                                         final String tableName, final ConfigurationProperties props) throws SQLException {
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        if (TableRefreshUtils.isSingleTable(tableName, database)) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceNames.iterator().next(), schemaName, tableName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                database.getProtocolType(), database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemaMap = GenericSchemaBuilder.build(Collections.singletonList(tableName), material);
        return Optional.ofNullable(schemaMap.get(schemaName)).map(optional -> optional.getTable(tableName))
                .orElseGet(() -> new ShardingSphereTable(tableName, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
    
    @Override
    public Class<AlterTableStatement> getType() {
        return AlterTableStatement.class;
    }
}
