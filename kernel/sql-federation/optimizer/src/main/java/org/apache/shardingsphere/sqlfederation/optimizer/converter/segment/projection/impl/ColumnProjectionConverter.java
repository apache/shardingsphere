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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.projection.impl;

import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ColumnConverter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Column projection converter. 
 */
public final class ColumnProjectionConverter implements SQLSegmentConverter<ColumnProjectionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ColumnProjectionSegment segment) {
        if (segment.getAlias().isPresent()) {
            Optional<SqlNode> column = new ColumnConverter().convert(segment.getColumn());
            SqlIdentifier alias = new SqlIdentifier(segment.getAlias().get(), SqlParserPos.ZERO);
            return column.map(optional -> new SqlBasicCall(new SqlAsOperator(), Arrays.asList(optional, alias), SqlParserPos.ZERO));
        }
        return new ColumnConverter().convert(segment.getColumn());
    }
}
