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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type.SelectStatementConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Subquery projection converter. 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryProjectionConverter {
    
    /**
     * Convert subquery projection segment to SQL node.
     *
     * @param segment subquery projection segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final SubqueryProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        SqlNode sqlNode = new SelectStatementConverter().convert(segment.getSubquery().getSelect());
        if (segment.getAliasName().isPresent()) {
            sqlNode = convertWithAlias(sqlNode, segment.getAliasName().get());
        }
        return segment.getSubquery().getSelect().getSubqueryType().map(optional -> optional == SubqueryType.EXISTS).orElse(false)
                ? Optional.of(new SqlBasicCall(SqlStdOperatorTable.EXISTS, Collections.singletonList(sqlNode), SqlParserPos.ZERO))
                : Optional.of(sqlNode);
    }
    
    private static SqlNode convertWithAlias(final SqlNode sqlNode, final String alias) {
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(sqlNode);
        sqlNodes.add(new SqlIdentifier(alias, SqlParserPos.ZERO));
        return new SqlBasicCall(SqlStdOperatorTable.AS, new ArrayList<>(sqlNodes), SqlParserPos.ZERO);
    }
}
