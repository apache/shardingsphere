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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.TableConverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Delete multi table converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteMultiTableConverter {
    
    /**
     * Convert delete multi table segment to sql node.
     * 
     * @param segment delete multi table segment
     * @return sql node
     */
    public static Optional<SqlNode> convert(final DeleteMultiTableSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        TableConverter.convert(segment.getRelationTable()).ifPresent(sqlNodes::add);
        List<String> tableNames = new LinkedList<>();
        for (SimpleTableSegment each : segment.getActualDeleteTables()) {
            tableNames.add(each.getTableName().getIdentifier().getValue());
        }
        sqlNodes.add(new SqlIdentifier(tableNames, SqlParserPos.ZERO));
        return Optional.of(new SqlNodeList(sqlNodes, SqlParserPos.ZERO));
    }
}
