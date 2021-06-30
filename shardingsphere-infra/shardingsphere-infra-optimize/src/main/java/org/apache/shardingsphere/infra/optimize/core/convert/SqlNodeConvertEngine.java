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

package org.apache.shardingsphere.infra.optimize.core.convert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.impl.SelectStatementSqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Optional;

/**
 * SqlNode convert engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlNodeConvertEngine {
    
    /**
     *  Convert.
     * @param statement statement
     * @return sqlNode optional
     */
    public static SqlNode convert(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            Optional<SqlNode> selectSqlNode = new SelectStatementSqlNodeConverter().convert((SelectStatement) statement);
            if (selectSqlNode.isPresent()) {
                return selectSqlNode.get();
            }
        }
        throw new UnsupportedOperationException("Unsupported sqlNode conversion.");
    }
}
