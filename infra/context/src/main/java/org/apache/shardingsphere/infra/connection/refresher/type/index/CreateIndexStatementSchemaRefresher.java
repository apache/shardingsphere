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

package org.apache.shardingsphere.infra.connection.refresher.type.index;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;

import java.util.Collection;

/**
 * Schema refresher for create index statement.
 */
public final class CreateIndexStatementSchemaRefresher implements MetaDataRefresher<CreateIndexStatement> {
    
    @Override
    public void refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final CreateIndexStatement sqlStatement, final ConfigurationProperties props) {
        String indexName = null != sqlStatement.getIndex() ? sqlStatement.getIndex().getIndexName().getIdentifier().getValue()
                : IndexMetaDataUtils.getGeneratedLogicIndexName(sqlStatement.getColumns());
        if (Strings.isNullOrEmpty(indexName)) {
            return;
        }
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        ShardingSphereTable table = newShardingSphereTable(database.getSchema(schemaName).getTable(tableName));
        table.putIndex(new ShardingSphereIndex(indexName));
        AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), schemaName);
        alterSchemaMetaDataPOJO.getAlteredTables().add(table);
        modeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
    }
    
    private ShardingSphereTable newShardingSphereTable(final ShardingSphereTable table) {
        ShardingSphereTable result = new ShardingSphereTable(table.getName(), table.getColumnValues(), table.getIndexValues(), table.getConstraintValues());
        result.getColumnNames().addAll(table.getColumnNames());
        result.getVisibleColumns().addAll(table.getVisibleColumns());
        result.getPrimaryKeyColumns().addAll(table.getPrimaryKeyColumns());
        return result;
    }
    
    @Override
    public Class<CreateIndexStatement> getType() {
        return CreateIndexStatement.class;
    }
}
