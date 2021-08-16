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

package org.apache.shardingsphere.sql.parser.sql.common.segment.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;

import java.util.LinkedList;
import java.util.List;

/**
 * Model segment.
 */
@RequiredArgsConstructor
@Getter
public final class ModelSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private List<SubquerySegment> referenceModelSelect = new LinkedList<>();
    
    private List<OrderBySegment> orderBySegments = new LinkedList<>();
    
    private List<ColumnSegment> cellAssignmentColumns = new LinkedList<>();
    
    private List<SubquerySegment> cellAsssignmentSelect = new LinkedList<>();
}
