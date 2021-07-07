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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;

import java.util.Optional;

/**
 * Simple table converter.
 */
public final class SimpleTableSqlNodeConverter implements SqlNodeConverter<SimpleTableSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final SimpleTableSegment simpleTable) {
        TableNameSegment tableName = simpleTable.getTableName();
        SqlNode tableNameSqlNode = new SqlIdentifier(tableName.getIdentifier().getValue(), SqlParserPos.ZERO);
        SqlNode sqlNode;
        if (simpleTable.getAlias().isPresent()) {
            SqlNode aliasIdentifier = new SqlIdentifier(simpleTable.getAlias().get(), SqlParserPos.ZERO);
            sqlNode = new SqlBasicCall(SqlStdOperatorTable.AS, new SqlNode[] {tableNameSqlNode, aliasIdentifier}, SqlParserPos.ZERO);
        } else {
            sqlNode = tableNameSqlNode;
        }
        return Optional.of(sqlNode);
    }
}
