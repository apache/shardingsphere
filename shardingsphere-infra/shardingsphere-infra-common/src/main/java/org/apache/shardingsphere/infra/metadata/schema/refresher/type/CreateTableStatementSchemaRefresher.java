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

package org.apache.shardingsphere.infra.metadata.schema.refresher.type;

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.DefaultTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import java.sql.SQLException;
import java.util.Collection;

/**
 * ShardingSphere schema refresher for create table statement.
 */
public final class CreateTableStatementSchemaRefresher implements SchemaRefresher<CreateTableStatement> {
    
    @Override
    public void refresh(final ShardingSphereSchema schema, final Collection<String> logicDataSourceNames, 
                        final CreateTableStatement sqlStatement, final SchemaBuilderMaterials materials) throws SQLException {
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        if (!containsInDataNodeContainedRule(tableName, materials)) {
            findShardingSphereRulesByClass(materials.getRules(), SingleTableRule.class).forEach(each -> each.addSingleTableDataNode(tableName, logicDataSourceNames.iterator().next()));
        }
        TableMetaData tableMetaData;
        if (containsInTableContainedRule(tableName, materials)) {
            tableMetaData = TableMetaDataBuilder.build(tableName, materials).orElseGet(TableMetaData::new);
        } else {
            tableMetaData = DefaultTableMetaDataLoader.load(tableName, logicDataSourceNames, materials).orElseGet(TableMetaData::new);
        }
        schema.put(tableName, tableMetaData);
    }
    
    private boolean containsInDataNodeContainedRule(final String tableName, final SchemaBuilderMaterials materials) {
        return findShardingSphereRulesByClass(materials.getRules(), DataNodeContainedRule.class).stream().anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    private boolean containsInTableContainedRule(final String tableName, final SchemaBuilderMaterials materials) {
        return findShardingSphereRulesByClass(materials.getRules(), TableContainedRule.class).stream().anyMatch(each -> each.getTables().contains(tableName));
    }
}
