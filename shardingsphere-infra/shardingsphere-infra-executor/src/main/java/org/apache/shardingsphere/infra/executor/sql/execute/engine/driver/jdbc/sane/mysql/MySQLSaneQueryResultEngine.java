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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.mysql;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.JDBCSaneQueryResultEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect.DatabaseMetaDataDialectHandler;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect.DatabaseMetaDataDialectHandlerFactory;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Sane query result engine for MySQL.
 */
public final class MySQLSaneQueryResultEngine implements JDBCSaneQueryResultEngine {
    
    @Override
    public Optional<QueryResult> getSaneQueryResult(final SQLStatement sqlStatement, final JDBCExecutionUnit jdbcExecutionUnit, final DatabaseType targetDatabaseType) throws SQLException {
        Optional<String> saneSQL = getSaneSQL(sqlStatement, getDialectQuoteCharacter(targetDatabaseType));
        return saneSQL.isPresent() ? Optional.of(new JDBCMemoryQueryResult(jdbcExecutionUnit.getStorageResource().executeQuery(saneSQL.get()))) : Optional.empty();
    }
    
    private QuoteCharacter getDialectQuoteCharacter(final DatabaseType targetDatabaseType) {
        Optional<DatabaseMetaDataDialectHandler> databaseMetaDataDialectHandler = DatabaseMetaDataDialectHandlerFactory.findHandler(targetDatabaseType);
        return databaseMetaDataDialectHandler.isPresent() ? databaseMetaDataDialectHandler.get().getQuoteCharacter() : QuoteCharacter.NONE;
    }
    
    private Optional<String> getSaneSQL(final SQLStatement sqlStatement, final QuoteCharacter quoteCharacter) {
        if (sqlStatement instanceof SelectStatement) {
            return Optional.of(getSaneSQL((SelectStatement) sqlStatement, quoteCharacter));
        } else if (sqlStatement instanceof MySQLShowOtherStatement) {
            return Optional.of("SELECT 1");
        }
        return Optional.empty();
    }
    
    private String getSaneSQL(final SelectStatement selectStatement, final QuoteCharacter quoteCharacter) {
        Collection<String> saneProjections = new LinkedList<>();
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            if (each instanceof ExpressionProjectionSegment) {
                String alias = ((ExpressionProjectionSegment) each).getAlias().orElse(((ExpressionProjectionSegment) each).getText());
                saneProjections.add(String.format("'%s' AS %s", MySQLDefaultVariable.containsVariable(alias) ? MySQLDefaultVariable.getVariable(alias) : "1", quoteCharacter.wrap(alias)));
            }
        }
        return String.format("SELECT %s", String.join(", ", saneProjections));
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
