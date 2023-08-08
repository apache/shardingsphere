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

import org.apache.shardingsphere.distsql.handler.ral.query.DatabaseRequiredQueryableRALExecutor;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Export database configuration executor.
 */
public final class ExportDatabaseConfigurationExecutor implements DatabaseRequiredQueryableRALExecutor<ExportDatabaseConfigurationStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singleton("result");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ExportDatabaseConfigurationStatement sqlStatement) {
        String exportedData = ExportUtils.generateExportDatabaseData(database);
        if (sqlStatement.getFilePath().isPresent()) {
            String filePath = sqlStatement.getFilePath().get();
            ExportUtils.exportToFile(filePath, exportedData);
            return Collections.singleton(new LocalDataQueryResultRow(String.format("Successfully exported to：'%s'", filePath)));
        }
        return Collections.singleton(new LocalDataQueryResultRow(exportedData));
    }
    
    @Override
    public Class<ExportDatabaseConfigurationStatement> getType() {
        return ExportDatabaseConfigurationStatement.class;
    }
}
