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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.window;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlWindow;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Window converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowConverter {
    
    /**
     * Convert window segment to sql node list.
     * 
     * @param segment window segment
     * @return sql node list
     */
    public static Optional<SqlNodeList> convert(final WindowSegment segment) {
        Collection<SqlWindow> sqlWindows = new LinkedList<>();
        for (WindowItemSegment each : segment.getItemSegments()) {
            SqlIdentifier sqlIdentifier = null == each.getWindowName() ? new SqlIdentifier("", SqlParserPos.ZERO) : new SqlIdentifier(each.getWindowName().getValue(), SqlParserPos.ZERO);
            Collection<SqlNode> partitionNodes = new LinkedList<>();
            each.getPartitionListSegments().forEach(expressionSegment -> ExpressionConverter.convert(expressionSegment).ifPresent(partitionNodes::add));
            SqlNodeList partitionList = new SqlNodeList(partitionNodes, SqlParserPos.ZERO);
            SqlNodeList orderList = new SqlNodeList(SqlParserPos.ZERO);
            SqlWindow sqlWindow = new SqlWindow(SqlParserPos.ZERO, sqlIdentifier, null, partitionList, orderList, SqlLiteral.createBoolean(false, SqlParserPos.ZERO), null, null, null);
            sqlWindows.add(sqlWindow);
        }
        SqlNodeList result = new SqlNodeList(sqlWindows, SqlParserPos.ZERO);
        return Optional.of(result);
    }
}
