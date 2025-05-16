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

package org.apache.shardingsphere.infra.binder.context.statement.dal;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Explain statement context.
 */
@Getter
public final class ExplainStatementContext extends CommonSQLStatementContext implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    private final SQLStatementContext sqlStatementContext;
    
    public ExplainStatementContext(final ShardingSphereMetaData metaData, final ExplainStatement sqlStatement, final List<Object> params, final String currentDatabaseName) {
        super(sqlStatement);
        tablesContext = new TablesContext(extractTablesFromExplain(sqlStatement));
        sqlStatementContext = SQLStatementContextFactory.newInstance(metaData, sqlStatement.getSqlStatement(), params, currentDatabaseName);
    }
    
    private Collection<SimpleTableSegment> extractTablesFromExplain(final ExplainStatement sqlStatement) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        sqlStatement.getSimpleTable().ifPresent(result::add);
        SQLStatement explainableStatement = sqlStatement.getSqlStatement();
        TableExtractor extractor = new TableExtractor();
        // TODO extract table from declare, execute, createMaterializedView, refreshMaterializedView
        extractor.extractTablesFromSQLStatement(explainableStatement);
        result.addAll(extractor.getRewriteTables());
        return result;
    }
    
    @Override
    public ExplainStatement getSqlStatement() {
        return (ExplainStatement) super.getSqlStatement();
    }
}
