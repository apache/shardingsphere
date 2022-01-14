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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTableMetadataStatement;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Show table metadata executor.
 */
@RequiredArgsConstructor
public final class ShowTableMetadataExecutor extends AbstractShowExecutor {
    
    private static final String SCHEMA_NAME = "schema_name";
    
    private static final String TABLE_NAME = "table_name";
    
    private static final String TYPE = "type";
    
    private static final String NAME = "name";
    
    private final ShowTableMetadataStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", SCHEMA_NAME, SCHEMA_NAME, Types.VARCHAR, "VARCHAR", 128, 0, false, false, false, false),
                new QueryHeader("", "", TABLE_NAME, TABLE_NAME, Types.VARCHAR, "VARCHAR", 128, 0, false, false, false, false),
                new QueryHeader("", "", TYPE, TYPE, Types.VARCHAR, "VARCHAR", 128, 0, false, false, false, false),
                new QueryHeader("", "", NAME, NAME, Types.VARCHAR, "VARCHAR", 128, 0, false, false, false, false)
        );
    }
    
    @Override
    protected MergedResult createMergedResult() {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
        ShardingSphereSchema schema = ProxyContext.getInstance().getMetaData(schemaName).getSchema();
        Collection<List<Object>> rows = schema.getAllTableNames().stream().filter(each -> sqlStatement.getTableNames().contains(each))
                .map(each -> buildTableRows(schemaName, schema, each)).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
        return new MultipleLocalDataMergedResult(rows);
    }
    
    private Collection<List<Object>> buildTableRows(final String schemaName, final ShardingSphereSchema schema, final String tableName) {
        Collection<List<Object>> result = new LinkedList<>();
        Collection<List<Object>> columnRows = schema.getAllColumnNames(tableName).stream().map(each -> buildRow(schemaName, tableName, "COLUMN", each))
                .collect(Collectors.toCollection(LinkedList::new));
        Collection<List<Object>> indexRows = schema.getTables().get(tableName).getIndexes().values().stream().map(each -> each.getName())
                .map(each -> buildRow(schemaName, tableName, "INDEX", each)).collect(Collectors.toCollection(LinkedList::new));
        result.addAll(columnRows);
        result.addAll(indexRows);
        return result;
    }
    
    private List<Object> buildRow(final String schemaName, final String tableName, final String type, final String name) {
        return new ArrayList<>(Arrays.asList(schemaName, tableName, type, name));
    }
    
}
