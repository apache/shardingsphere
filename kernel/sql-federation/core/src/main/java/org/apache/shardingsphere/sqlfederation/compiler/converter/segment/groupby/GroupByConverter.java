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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.groupby;

import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.orderby.item.OrderByItemConverterUtils;

import java.util.Optional;

/**
 * Group by converter.
 */
public final class GroupByConverter implements SQLSegmentConverter<GroupBySegment, SqlNodeList> {
    
    @Override
    public Optional<SqlNodeList> convert(final GroupBySegment segment) {
        return null == segment || segment.getGroupByItems().isEmpty()
                ? Optional.empty()
                : Optional.of(new SqlNodeList(OrderByItemConverterUtils.convert(segment.getGroupByItems()), SqlParserPos.ZERO));
    }
}
