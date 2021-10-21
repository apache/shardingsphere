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

package org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Arrays;
import java.util.Optional;

/**
 * Column converter.
 */
public final class ColumnConverter implements SQLSegmentConverter<ColumnSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final ColumnSegment segment) {
        Optional<OwnerSegment> owner = segment.getOwner();
        String columnName = segment.getIdentifier().getValue();
        SqlIdentifier sqlIdentifier = owner.map(optional 
            -> new SqlIdentifier(Arrays.asList(optional.getIdentifier().getValue(), columnName), SqlParserPos.ZERO)).orElseGet(() -> new SqlIdentifier(columnName, SqlParserPos.ZERO));
        return Optional.of(sqlIdentifier);
    }
    
    @Override
    public Optional<ColumnSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlIdentifier) {
            ImmutableList<String> names = ((SqlIdentifier) sqlNode).names;
            if (1 == names.size()) {
                return Optional.of(new ColumnSegment(sqlNode.getParserPosition().getColumnNum() - 1, sqlNode.getParserPosition().getEndColumnNum() - 1, new IdentifierValue(names.get(0))));    
            }
            ColumnSegment columnSegment = new ColumnSegment(sqlNode.getParserPosition().getColumnNum(), sqlNode.getParserPosition().getEndColumnNum(), new IdentifierValue(names.get(1)));
            columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue(names.get(0))));
            return Optional.of(columnSegment);
        }
        return Optional.empty();
    }
}
