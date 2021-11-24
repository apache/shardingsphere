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

package org.apache.shardingsphere.infra.federation.converter.segment.projection.impl;

import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.List;
import java.util.Optional;

/**
 * Column projection converter. 
 */
public final class ColumnProjectionConverter implements SQLSegmentConverter<ColumnProjectionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final ColumnProjectionSegment segment) {
        if (segment.getAlias().isPresent()) {
            Optional<SqlIdentifier> columnSqlIdentifier = new ColumnConverter().convertToSQLNode(segment.getColumn());
            SqlIdentifier aliasSqlIdentifier = new SqlIdentifier(segment.getAlias().get(), SqlParserPos.ZERO);
            return Optional.of(new SqlBasicCall(new SqlAsOperator(), new SqlNode[]{columnSqlIdentifier.get(), aliasSqlIdentifier}, SqlParserPos.ZERO));
        }
        return new ColumnConverter().convertToSQLNode(segment.getColumn()).map(optional -> optional);
    }
    
    @Override
    public Optional<ColumnProjectionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlBasicCall) {
            List<SqlNode> operands = ((SqlBasicCall) sqlNode).getOperandList();
            Optional<ColumnSegment> columnSegment = new ColumnConverter().convertToSQLSegment((SqlIdentifier) operands.get(0));
            if (!columnSegment.isPresent()) {
                return Optional.empty();
            }
            ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment.get());
            if (2 == operands.size()) {
                SqlIdentifier aliasSqlNode = (SqlIdentifier) operands.get(1);
                columnProjectionSegment.setAlias(new AliasSegment(getStartIndex(aliasSqlNode), getStopIndex(aliasSqlNode), new IdentifierValue(aliasSqlNode.names.get(0))));
            }
            return Optional.of(columnProjectionSegment);
        }
        if (sqlNode instanceof SqlIdentifier) {
            return new ColumnConverter().convertToSQLSegment((SqlIdentifier) sqlNode).map(ColumnProjectionSegment::new);
        }
        return Optional.empty();
    }
}
