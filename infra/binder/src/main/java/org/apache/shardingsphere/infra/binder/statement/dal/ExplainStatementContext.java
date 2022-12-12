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

package org.apache.shardingsphere.infra.binder.statement.dal;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dal.ExplainStatementHandler;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Explain statement context.
 */
@Getter
public final class ExplainStatementContext extends CommonSQLStatementContext<ExplainStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    public ExplainStatementContext(final ExplainStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(extractTablesFromExplain(sqlStatement), getDatabaseType());
    }
    
    private Collection<SimpleTableSegment> extractTablesFromExplain(final ExplainStatement sqlStatement) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        ExplainStatementHandler.getSimpleTableSegment(sqlStatement).ifPresent(result::add);
        SQLStatement explainableStatement = sqlStatement.getStatement().orElse(null);
        TableExtractor extractor = new TableExtractor();
        // TODO extract table from declare, execute, createMaterializedView, refreshMaterializedView
        extractor.extractTablesFromSQLStatement(explainableStatement);
        result.addAll(extractor.getRewriteTables());
        return result;
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return extractTablesFromExplain(getSqlStatement());
    }
}
