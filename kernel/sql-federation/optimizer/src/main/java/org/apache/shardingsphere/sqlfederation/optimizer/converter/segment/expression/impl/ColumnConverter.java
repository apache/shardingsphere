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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Column converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnConverter {
    
    /**
     * Convert column segment to sql node.
     * 
     * @param segment column segment
     * @return sql node
     */
    public static Optional<SqlNode> convert(final ColumnSegment segment) {
        List<String> names = new ArrayList<>();
        segment.getOwner().ifPresent(optional -> addOwnerNames(names, optional));
        names.add(segment.getIdentifier().getValue());
        return Optional.of(new SqlIdentifier(names, SqlParserPos.ZERO));
    }
    
    private static void addOwnerNames(final List<String> names, final OwnerSegment owner) {
        owner.getOwner().ifPresent(optional -> addOwnerNames(names, optional));
        names.add(owner.getIdentifier().getValue());
    }
}
