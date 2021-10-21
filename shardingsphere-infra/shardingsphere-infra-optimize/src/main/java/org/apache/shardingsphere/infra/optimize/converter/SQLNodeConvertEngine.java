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

package org.apache.shardingsphere.infra.optimize.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.shardingsphere.infra.optimize.converter.statement.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * SQL node convert engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLNodeConvertEngine {
    
    /**
     * Convert SQL statement to SQL node.
     * 
     * @param statement SQL statement to be converted
     * @return sqlNode converted SQL node
     */
    public static SqlNode convertToSQLNode(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return new SelectStatementConverter().convertToSQLNode((SelectStatement) statement);
        }
        throw new UnsupportedOperationException("Unsupported SQL node conversion.");
    }
    
    /**
     * Convert SQL node to SQL statement.
     *
     * @param sqlNode sqlNode converted SQL node
     * @return SQL statement to be converted
     */
    public static SQLStatement convertToSQLStatement(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlOrderBy || sqlNode instanceof SqlSelect) {
            return new SelectStatementConverter().convertToSQLStatement(sqlNode);
        }
        throw new UnsupportedOperationException("Unsupported SQL statement conversion.");
    }
}
