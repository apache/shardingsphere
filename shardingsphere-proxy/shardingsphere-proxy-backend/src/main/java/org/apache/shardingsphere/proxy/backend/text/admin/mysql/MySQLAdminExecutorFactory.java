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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowCurrentDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.UseDatabaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.util.Optional;

/**
 * Admin executor factory for MySQL.
 */
public final class MySQLAdminExecutorFactory implements DatabaseAdminExecutorFactory {
    
    private static final String INFORMATION_SCHEMA = "information_schema";
    
    private static final String PERFORMANCE_SCHEMA = "performance_schema";
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final String currentSchema, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof UseStatement) {
            return Optional.of(new UseDatabaseExecutor((UseStatement) sqlStatement));
        }
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return Optional.of(new ShowDatabasesExecutor());
        }
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            return Optional.of(new ShowTablesExecutor());
        }
        if (sqlStatement instanceof MySQLShowProcessListStatement) {
            return Optional.of(new ShowProcessListExecutor());
        }
        if (sqlStatement instanceof SelectStatement) {
            if (isShowCurrentDatabaseStatement((SelectStatement) sqlStatement)) {
                return Optional.of(new ShowCurrentDatabaseExecutor());
            } 
            if (isQueryInformationSchema(currentSchema, (SelectStatement) sqlStatement)) {
                // TODO
                return Optional.empty();
            }
            if (isQueryPerformanceSchema(currentSchema, (SelectStatement) sqlStatement)) {
                // TODO
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private boolean isShowCurrentDatabaseStatement(final SelectStatement sqlStatement) {
        ProjectionSegment firstProjection = sqlStatement.getProjections().getProjections().iterator().next();
        return firstProjection instanceof ExpressionProjectionSegment && ShowCurrentDatabaseExecutor.FUNCTION_NAME.equalsIgnoreCase(((ExpressionProjectionSegment) firstProjection).getText());
    }
    
    private boolean isQueryInformationSchema(final String currentSchema, final SelectStatement sqlStatement) {
        return isQuerySpecialSchema(currentSchema, sqlStatement, INFORMATION_SCHEMA);
    }
    
    private boolean isQueryPerformanceSchema(final String currentSchema, final SelectStatement sqlStatement) {
        return isQuerySpecialSchema(currentSchema, sqlStatement, PERFORMANCE_SCHEMA);
    }
    
    private boolean isQuerySpecialSchema(final String currentSchema, final SelectStatement sqlStatement, final String specialSchemaName) {
        TableSegment tableSegment = sqlStatement.getFrom();
        if (!(tableSegment instanceof SimpleTableSegment)) {
            return false;
        }
        if (specialSchemaName.equalsIgnoreCase(currentSchema) && !((SimpleTableSegment) tableSegment).getOwner().isPresent()) {
            return true;
        }
        return ((SimpleTableSegment) tableSegment).getOwner().isPresent() && specialSchemaName.equalsIgnoreCase(((SimpleTableSegment) tableSegment).getOwner().get().getIdentifier().getValue());
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
