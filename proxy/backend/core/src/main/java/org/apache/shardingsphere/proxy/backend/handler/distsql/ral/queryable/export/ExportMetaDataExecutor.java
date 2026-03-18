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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.export;

import org.apache.commons.codec.binary.Base64;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportMetaDataStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.util.ClusterExportMetaDataGenerator;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Export metadata executor.
 */
public final class ExportMetaDataExecutor implements DistSQLQueryExecutor<ExportMetaDataStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ExportMetaDataStatement sqlStatement) {
        return Arrays.asList("id", "create_time", "cluster_info");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ExportMetaDataStatement sqlStatement, final ContextManager contextManager) {
        String exportedData = new ClusterExportMetaDataGenerator(contextManager).generateJsonFormat();
        String instanceId = contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId();
        if (sqlStatement.getFilePath().isPresent()) {
            String filePath = sqlStatement.getFilePath().get();
            ExportUtils.exportToFile(filePath, exportedData);
            return Collections.singleton(new LocalDataQueryResultRow(instanceId, LocalDateTime.now(), String.format("Successfully exported to：'%s'", filePath)));
        }
        return Collections.singleton(new LocalDataQueryResultRow(instanceId, LocalDateTime.now(), Base64.encodeBase64String(exportedData.getBytes())));
    }
    
    @Override
    public Class<ExportMetaDataStatement> getType() {
        return ExportMetaDataStatement.class;
    }
}
