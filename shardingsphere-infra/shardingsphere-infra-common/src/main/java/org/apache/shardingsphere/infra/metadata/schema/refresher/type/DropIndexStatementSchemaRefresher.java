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
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropIndexStatementHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere schema refresher for drop index statement.
 */
public final class DropIndexStatementSchemaRefresher implements SchemaRefresher<DropIndexStatement> {
    
    @Override
    public void refresh(final ShardingSphereSchema schema, 
                        final Collection<String> routeDataSourceNames, final DropIndexStatement sqlStatement, final SchemaBuilderMaterials materials) {
        Collection<String> indexNames = getIndexNames(sqlStatement);
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(sqlStatement);
        String tableName = simpleTableSegment.map(tableSegment -> tableSegment.getTableName().getIdentifier().getValue()).orElse("");
        TableMetaData tableMetaData = schema.get(tableName);
        if (simpleTableSegment.isPresent()) {
            indexNames.forEach(each -> tableMetaData.getIndexes().remove(each));
        }
        for (String each : indexNames) {
            if (findLogicTableName(schema, each).isPresent()) {
                tableMetaData.getIndexes().remove(each);
            }
        }
    }
    
    private Collection<String> getIndexNames(final DropIndexStatement dropIndexStatement) {
        return dropIndexStatement.getIndexes().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Optional<String> findLogicTableName(final ShardingSphereSchema schema, final String logicIndexName) {
        return schema.getAllTableNames().stream().filter(each -> schema.get(each).getIndexes().containsKey(logicIndexName)).findFirst();
    }
}
