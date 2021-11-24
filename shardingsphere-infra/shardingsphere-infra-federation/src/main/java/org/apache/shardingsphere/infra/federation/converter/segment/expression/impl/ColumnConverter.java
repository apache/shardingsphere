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

package org.apache.shardingsphere.infra.federation.converter.segment.expression.impl;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Arrays;
import java.util.Optional;

/**
 * Column converter.
 */
public final class ColumnConverter implements SQLSegmentConverter<ColumnSegment, SqlIdentifier> {
    
    @Override
    public Optional<SqlIdentifier> convertToSQLNode(final ColumnSegment segment) {
        Optional<OwnerSegment> owner = segment.getOwner();
        String columnName = segment.getIdentifier().getValue();
        SqlIdentifier sqlIdentifier = owner.map(optional 
            -> new SqlIdentifier(Arrays.asList(optional.getIdentifier().getValue(), columnName), SqlParserPos.ZERO)).orElseGet(() -> new SqlIdentifier(columnName, SqlParserPos.ZERO));
        return Optional.of(sqlIdentifier);
    }
    
    @Override
    public Optional<ColumnSegment> convertToSQLSegment(final SqlIdentifier sqlIdentifier) {
        if (null == sqlIdentifier) {
            return Optional.empty();
        }
        ImmutableList<String> names = sqlIdentifier.names;
        if (1 == names.size()) {
            return Optional.of(new ColumnSegment(getStartIndex(sqlIdentifier), getStopIndex(sqlIdentifier), new IdentifierValue(names.get(0))));
        }
        ColumnSegment result = new ColumnSegment(getStartIndex(sqlIdentifier), getStopIndex(sqlIdentifier), new IdentifierValue(names.get(1)));
        SqlIdentifier owner = sqlIdentifier.getComponent(0);
        result.setOwner(new OwnerSegment(getStartIndex(owner), getStopIndex(owner), new IdentifierValue(names.get(0))));
        return Optional.of(result);
    }
}
