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

package org.apache.shardingsphere.infra.optimize.convert.converter.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.convert.converter.SQLNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Projection converter.
 */
public final class ProjectionsSQLNodeConverter implements SQLNodeConverter<ProjectionsSegment, SqlNodeList> {
    
    @Override
    public Optional<SqlNodeList> convert(final ProjectionsSegment projectionsSegment) {
        Collection<ProjectionSegment> projections = projectionsSegment.getProjections();
        List<SqlNode> columnNodes = new ArrayList<>(projections.size());
        for (ProjectionSegment projection : projections) {
            Optional<SqlNode> optional = Optional.empty();
            if (projection instanceof ColumnProjectionSegment) {
                optional = new ColumnProjectionSQLNodeConverter().convert((ColumnProjectionSegment) projection);
            } else if (projection instanceof ExpressionProjectionSegment) {
                optional = new ExpressionProjectionSQLNodeConverter().convert((ExpressionProjectionSegment) projection);
            }
            // TODO other Projection
            if (optional.isPresent()) {
                columnNodes.add(optional.get());
            }
        }
        return Optional.of(new SqlNodeList(columnNodes, SqlParserPos.ZERO));
    }
}
