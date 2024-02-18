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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.export.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Export database configuration executor.
 */
@Setter
public final class ExportDatabaseConfigurationExecutor implements DistSQLQueryExecutor<ExportDatabaseConfigurationStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final ExportDatabaseConfigurationStatement sqlStatement) {
        return Collections.singleton("result");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ExportDatabaseConfigurationStatement sqlStatement, final ContextManager contextManager) {
        String exportedData = ExportUtils.generateExportDatabaseData(database);
        if (!sqlStatement.getFilePath().isPresent()) {
            return Collections.singleton(new LocalDataQueryResultRow(exportedData));
        }
        String filePath = sqlStatement.getFilePath().get();
        ExportUtils.exportToFile(filePath, exportedData);
        return Collections.singleton(new LocalDataQueryResultRow(String.format("Successfully exported to：'%s'", filePath)));
    }
    
    @Override
    public void setDatabase(final ShardingSphereDatabase database) {
        this.database = database;
    }
    
    @Override
    public Class<ExportDatabaseConfigurationStatement> getType() {
        return ExportDatabaseConfigurationStatement.class;
    }
}
