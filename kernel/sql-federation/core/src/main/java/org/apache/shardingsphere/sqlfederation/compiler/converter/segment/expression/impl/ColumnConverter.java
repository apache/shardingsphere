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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Column converter.
 */
public final class ColumnConverter implements SQLSegmentConverter<ColumnSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ColumnSegment segment) {
        Optional<OwnerSegment> owner = segment.getOwner();
        String columnName = segment.getIdentifier().getValue();
        SqlIdentifier sqlIdentifier = owner.map(optional -> new SqlIdentifier(Arrays.asList(optional.getIdentifier().getValue(), columnName), SqlParserPos.ZERO))
                .orElseGet(() -> new SqlIdentifier(columnName, SqlParserPos.ZERO));
        return Optional.of(sqlIdentifier);
    }
}
