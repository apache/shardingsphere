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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.from.impl;

import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;

import java.util.Optional;

/**
 * Join converter.
 */
public final class JoinTableConverter implements SQLSegmentConverter<JoinTableSegment, SqlJoin> {
    
    @Override
    public Optional<SqlJoin> convert(final JoinTableSegment segment) {
        SqlNode left = new TableConverter().convert(segment.getLeft()).orElseThrow(IllegalStateException::new);
        SqlNode right = new TableConverter().convert(segment.getRight()).orElseThrow(IllegalStateException::new);
        Optional<SqlNode> condition = new ExpressionConverter().convert(segment.getCondition());
        SqlLiteral conditionType = condition.isPresent() ? JoinConditionType.ON.symbol(SqlParserPos.ZERO) : JoinConditionType.NONE.symbol(SqlParserPos.ZERO);
        SqlLiteral joinType = JoinType.valueOf(segment.getJoinType()).symbol(SqlParserPos.ZERO);
        return Optional.of(new SqlJoin(SqlParserPos.ZERO, left, SqlLiteral.createBoolean(false, SqlParserPos.ZERO), joinType, right, conditionType, condition.orElse(null)));
    }
}
