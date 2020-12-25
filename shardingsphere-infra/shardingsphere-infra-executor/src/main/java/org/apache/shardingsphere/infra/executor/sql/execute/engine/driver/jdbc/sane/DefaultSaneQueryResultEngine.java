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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Default Sane query result engine.
 */
public final class DefaultSaneQueryResultEngine implements JDBCSaneQueryResultEngine {
    
    @Override
    public Optional<QueryResult> getSaneQueryResult(final SQLStatement sqlStatement, final JDBCExecutionUnit jdbcExecutionUnit, final DatabaseType targetDatabaseType) throws SQLException {
        return sqlStatement instanceof SelectStatement || sqlStatement instanceof MySQLShowOtherStatement
                ? Optional.of(new JDBCMemoryQueryResult(jdbcExecutionUnit.getStorageResource().executeQuery("SELECT 1"))) : Optional.empty();
    }
    
    @Override
    public String getType() {
        return "";
    }
}
