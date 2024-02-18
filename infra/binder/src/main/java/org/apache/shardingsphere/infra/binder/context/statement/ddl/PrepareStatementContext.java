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

package org.apache.shardingsphere.infra.binder.context.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.PrepareStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Prepare statement context.
 */
@Getter
public final class PrepareStatementContext extends CommonSQLStatementContext implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    public PrepareStatementContext(final PrepareStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(extractTablesFromPreparedStatement(sqlStatement), getDatabaseType());
    }
    
    @Override
    public PrepareStatement getSqlStatement() {
        return (PrepareStatement) super.getSqlStatement();
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return extractTablesFromPreparedStatement(getSqlStatement());
    }
    
    private Collection<SimpleTableSegment> extractTablesFromPreparedStatement(final PrepareStatement sqlStatement) {
        TableExtractor tableExtractor = new TableExtractor();
        sqlStatement.getSelect().ifPresent(tableExtractor::extractTablesFromSelect);
        sqlStatement.getInsert().ifPresent(tableExtractor::extractTablesFromInsert);
        sqlStatement.getUpdate().ifPresent(tableExtractor::extractTablesFromUpdate);
        sqlStatement.getDelete().ifPresent(tableExtractor::extractTablesFromDelete);
        return new LinkedList<>(tableExtractor.getRewriteTables());
    }
}
