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

package org.apache.shardingsphere.sqlfederation.compiler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.delete.DeleteStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.explain.ExplainStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.insert.InsertStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.update.UpdateStatementConverter;
import org.apache.shardingsphere.sqlfederation.exception.OptimizationSQLNodeConvertException;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.select.SelectStatementConverter;

/**
 * SQL node converter engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLNodeConverterEngine {
    
    /**
     * Convert SQL statement to SQL node.
     * 
     * @param statement SQL statement to be converted
     * @return sqlNode converted SQL node
     * @throws OptimizationSQLNodeConvertException optimization SQL node convert exception
     */
    public static SqlNode convert(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return new SelectStatementConverter().convert((SelectStatement) statement);
        }
        if (statement instanceof DeleteStatement) {
            return new DeleteStatementConverter().convert((DeleteStatement) statement);
        }
        if (statement instanceof ExplainStatement) {
            return new ExplainStatementConverter().convert((ExplainStatement) statement);
        }
        if (statement instanceof UpdateStatement) {
            return new UpdateStatementConverter().convert((UpdateStatement) statement);
        }
        if (statement instanceof InsertStatement) {
            return new InsertStatementConverter().convert((InsertStatement) statement);
        }
        throw new OptimizationSQLNodeConvertException(statement);
    }
}
