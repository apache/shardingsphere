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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type.SelectStatementConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Subquery table converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryTableConverter {
    
    /**
     * Convert subquery table segment to SQL node.
     *
     * @param segment subquery table segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final SubqueryTableSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        if (null == segment.getSubquery().getSelect().getProjections()) {
            List<SqlNode> tables = segment.getSubquery().getSelect().getFrom().flatMap(TableConverter::convert).map(Collections::singletonList).orElseGet(Collections::emptyList);
            sqlNodes.add(new SqlBasicCall(SqlStdOperatorTable.EXPLICIT_TABLE, tables, SqlParserPos.ZERO));
        } else {
            sqlNodes.add(new SelectStatementConverter().convert(segment.getSubquery().getSelect()));
        }
        segment.getAliasName().ifPresent(optional -> sqlNodes.add(new SqlIdentifier(optional, SqlParserPos.ZERO)));
        return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, new ArrayList<>(sqlNodes), SqlParserPos.ZERO));
    }
}
