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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.generic.OwnerConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Simple table converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTableConverter {
    
    /**
     * Convert simple table segment to sql node.
     *
     * @param segment simple table segment
     * @return sql node
     */
    public static Optional<SqlNode> convert(final SimpleTableSegment segment) {
        if ("DUAL".equalsIgnoreCase(segment.getTableName().getIdentifier().getValue())) {
            return Optional.empty();
        }
        TableNameSegment tableName = segment.getTableName();
        List<String> names = segment.getOwner().isPresent() ? OwnerConverter.convert(segment.getOwner().get()) : new ArrayList<>();
        names.add(tableName.getIdentifier().getValue());
        if (segment.getDbLink().isPresent() && segment.getAt().isPresent()) {
            names.add(segment.getAt().get().getValue());
            names.add(segment.getDbLink().get().getValue());
        }
        SqlNode tableNameSQLNode = new SqlIdentifier(names, SqlParserPos.ZERO);
        if (segment.getAliasName().isPresent()) {
            SqlNode aliasSQLNode = new SqlIdentifier(segment.getAliasName().get(), SqlParserPos.ZERO);
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, Arrays.asList(tableNameSQLNode, aliasSQLNode), SqlParserPos.ZERO));
        }
        return Optional.of(tableNameSQLNode);
    }
}
