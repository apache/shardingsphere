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

package org.apache.shardingsphere.distsql.handler.type.query.rql;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowSPIImplementationsStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Show SPI implementations executor.
 */
@Setter
public final class ShowSPIImplementationsExecutor implements DistSQLQueryExecutor<ShowSPIImplementationsStatement> {
    
    private Collection<String> columnNames;
    
    @Override
    public Collection<String> getColumnNames() {
        return null != columnNames ? columnNames : Arrays.asList("type", "type_aliases", "description");
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSPIImplementationsStatement sqlStatement, final ContextManager contextManager) {
        Optional<ShowSPIImplementationsBuilder> rowBuilder = TypedSPILoader.findService(ShowSPIImplementationsBuilder.class, sqlStatement.getType());
        if (!rowBuilder.isPresent()) {
            return Collections.emptyList();
        }
        columnNames = rowBuilder.get().getColumnNames();
        return rowBuilder.get().generateRows();
    }
    
    @Override
    public Class<ShowSPIImplementationsStatement> getType() {
        return ShowSPIImplementationsStatement.class;
    }
}
