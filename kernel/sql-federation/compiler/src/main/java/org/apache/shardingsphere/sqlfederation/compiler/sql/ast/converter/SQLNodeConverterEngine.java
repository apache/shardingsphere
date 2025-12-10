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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationSQLNodeConvertException;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.delete.DeleteStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.explain.ExplainStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.insert.InsertStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.merge.MergeStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.select.SelectStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.update.UpdateStatementConverter;

import java.util.Optional;

/**
 * SQL node converter engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLNodeConverterEngine {
    
    /**
     * Convert SQL statement to SQL node.
     *
     * @param sqlStatement SQL sqlStatement to be converted
     * @return sqlNode converted SQL node
     * @throws SQLFederationSQLNodeConvertException SQL federation SQL node convert exception
     */
    public static SqlNode convert(final SQLStatement sqlStatement) {
        Optional<SqlNode> result = Optional.empty();
        if (sqlStatement instanceof DMLStatement) {
            result = convert((DMLStatement) sqlStatement);
        } else if (sqlStatement instanceof DALStatement) {
            result = convert((DALStatement) sqlStatement);
        }
        return result.orElseThrow(() -> new SQLFederationSQLNodeConvertException(sqlStatement));
    }
    
    private static Optional<SqlNode> convert(final DMLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return Optional.of(new SelectStatementConverter().convert((SelectStatement) sqlStatement));
        }
        if (sqlStatement instanceof DeleteStatement) {
            return Optional.of(new DeleteStatementConverter().convert((DeleteStatement) sqlStatement));
        }
        if (sqlStatement instanceof UpdateStatement) {
            return Optional.of(new UpdateStatementConverter().convert((UpdateStatement) sqlStatement));
        }
        if (sqlStatement instanceof InsertStatement) {
            return Optional.of(new InsertStatementConverter().convert((InsertStatement) sqlStatement));
        }
        if (sqlStatement instanceof MergeStatement) {
            return Optional.of(new MergeStatementConverter().convert((MergeStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    private static Optional<SqlNode> convert(final DALStatement sqlStatement) {
        if (sqlStatement instanceof ExplainStatement) {
            return Optional.of(new ExplainStatementConverter().convert((ExplainStatement) sqlStatement));
        }
        return Optional.empty();
    }
}
