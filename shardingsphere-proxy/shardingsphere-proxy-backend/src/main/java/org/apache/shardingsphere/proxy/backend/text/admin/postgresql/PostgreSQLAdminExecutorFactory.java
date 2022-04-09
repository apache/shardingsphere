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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.PostgreSQLSetCharsetExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.SelectDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.SelectTableExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Admin executor factory for PostgreSQL.
 */
public final class PostgreSQLAdminExecutorFactory implements DatabaseAdminExecutorFactory {
    
    private static final String PG_TABLESPACE = "pg_tablespace";
    
    private static final String PG_DATABASE = "pg_database";
    
    private static final String PG_TRIGGER = "pg_trigger";
    
    private static final String PG_INHERITS = "pg_inherits";
    
    private static final String PG_CLASS = "pg_class";
    
    private static final String PG_PREFIX = "pg_";
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatementContext<?> sqlStatementContext) {
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatementContext<?> sqlStatementContext, final String sql, final String schemaName) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof SelectStatement) {
            Collection<String> selectedTableNames = getSelectedTableNames((SelectStatement) sqlStatement);
            if (selectedTableNames.contains(PG_DATABASE)) {
                return Optional.of(new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql));
            }
            if (isQueryPgTable(selectedTableNames)) {
                return Optional.of(new SelectTableExecutor(sql));
            }
            if (selectedTableNames.stream().anyMatch(each -> each.startsWith(PG_PREFIX))) {
                return Optional.of(new DefaultDatabaseMetadataExecutor(sql));
            }
        }
        if (sqlStatement instanceof SetStatement) {
            SetStatement setStatement = (SetStatement) sqlStatement;
            // TODO Consider refactoring this with SPI.
            switch (getSetConfigurationParameter(setStatement)) {
                case "client_encoding":
                    return Optional.of(new PostgreSQLSetCharsetExecutor(setStatement));
                case "extra_float_digits":
                case "application_name":
                    return Optional.of(connectionSession -> { });
                default:
            }
        }
        return Optional.empty();
    }
    
    private boolean isQueryPgTable(final Collection<String> selectedTableNames) {
        boolean isComplexQueryTable = selectedTableNames.contains(PG_CLASS) && selectedTableNames.contains(PG_TRIGGER) && selectedTableNames.contains(PG_INHERITS);
        return selectedTableNames.contains(PG_TABLESPACE) || isComplexQueryTable;
    }
    
    private Collection<String> getSelectedTableNames(final SelectStatement sqlStatement) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(sqlStatement);
        List<TableSegment> subQueryTableSegment = extractor.getTableContext().stream().filter(each -> each instanceof SubqueryTableSegment).map(each -> {
            TableExtractor subExtractor = new TableExtractor();
            subExtractor.extractTablesFromSelect(((SubqueryTableSegment) each).getSubquery().getSelect());
            return subExtractor.getTableContext();
        }).flatMap(Collection::stream).collect(Collectors.toList());
        extractor.getTableContext().addAll(subQueryTableSegment);
        return extractor.getTableContext().stream().filter(each -> each instanceof SimpleTableSegment)
                .map(each -> ((SimpleTableSegment) each).getTableName().getIdentifier().getValue()).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private String getSetConfigurationParameter(final SetStatement setStatement) {
        Iterator<VariableAssignSegment> iterator = setStatement.getVariableAssigns().iterator();
        return iterator.hasNext() ? iterator.next().getVariable().getVariable().toLowerCase() : "";
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
