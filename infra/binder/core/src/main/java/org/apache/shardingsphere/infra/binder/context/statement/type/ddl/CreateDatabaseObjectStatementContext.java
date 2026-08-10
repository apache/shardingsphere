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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Create database object statement context.
 */
@Getter
public final class CreateDatabaseObjectStatementContext implements SQLStatementContext {
    
    private final SQLStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final Collection<SQLStatementContext> sqlStatementContexts = new LinkedList<>();
    
    public CreateDatabaseObjectStatementContext(final CreateFunctionStatement sqlStatement, final String currentDatabaseName, final ShardingSphereMetaData metaData) {
        this(sqlStatement, sqlStatement.getSqlStatements(), currentDatabaseName, metaData);
    }
    
    public CreateDatabaseObjectStatementContext(final CreateTriggerStatement sqlStatement, final String currentDatabaseName, final ShardingSphereMetaData metaData) {
        this(sqlStatement, sqlStatement.getSqlStatements(), currentDatabaseName, metaData);
    }
    
    public CreateDatabaseObjectStatementContext(final CreateProcedureStatement sqlStatement, final String currentDatabaseName, final ShardingSphereMetaData metaData) {
        this(sqlStatement, sqlStatement.getSqlStatements(), currentDatabaseName, metaData);
    }
    
    private CreateDatabaseObjectStatementContext(final SQLStatement sqlStatement, final Collection<SQLStatementSegment> internalSqlStatements,
                                                 final String currentDatabaseName, final ShardingSphereMetaData metaData) {
        this.sqlStatement = sqlStatement;
        Collection<SimpleTableSegment> tables = new LinkedList<>(extractTables(sqlStatement));
        SQLBindEngine sqlBindEngine = new SQLBindEngine(metaData, currentDatabaseName, new HintValueContext());
        for (SQLStatementSegment each : internalSqlStatements) {
            SQLStatementContext sqlStatementContext = createInternalSQLStatementContext(sqlBindEngine, sqlStatement, each, currentDatabaseName, metaData);
            sqlStatementContexts.add(sqlStatementContext);
            tables.addAll(sqlStatementContext.getTablesContext().getSimpleTables());
        }
        tablesContext = new TablesContext(tables);
    }
    
    private SQLStatementContext createInternalSQLStatementContext(final SQLBindEngine sqlBindEngine, final SQLStatement sqlStatement, final SQLStatementSegment segment,
                                                                  final String currentDatabaseName, final ShardingSphereMetaData metaData) {
        return sqlStatement instanceof CreateProcedureStatement
                ? SQLStatementContextFactory.newInstance(metaData, segment.getSqlStatement(), currentDatabaseName)
                : sqlBindEngine.bind(segment.getSqlStatement());
    }
    
    private Collection<SimpleTableSegment> extractTables(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateProcedureStatement) {
            Optional<RoutineBodySegment> routineBody = ((CreateProcedureStatement) sqlStatement).getRoutineBody();
            return routineBody.map(optional -> new TableExtractor().extractExistTableFromRoutineBody(optional)).orElseGet(Collections::emptyList);
        }
        return sqlStatement.getAttributes().findAttribute(TableSQLStatementAttribute.class).map(TableSQLStatementAttribute::getTables).orElseGet(Collections::emptyList);
    }
}
