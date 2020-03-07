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

package org.apache.shardingsphere.underlying.rewrite.metadata;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Projection meta data.
 */
@Getter
public final class ProjectionMetaData {
    
    private final Collection<SimpleTableSegment> tableSegments;
    
    private final List<Projection> projections;
    
    public ProjectionMetaData() {
        tableSegments = Collections.emptyList();
        projections = Collections.emptyList();
    }
    
    public ProjectionMetaData(final SelectStatementContext selectStatementContext) {
        tableSegments = selectStatementContext.getSqlStatement().getTables();
        projections = selectStatementContext.getProjectionsContext().getActualProjections();
    }
}
