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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.groupby;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.HavingSegment;

import java.util.Optional;

/**
 * Having converter.
 */
public final class HavingConverter implements SQLSegmentConverter<HavingSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final HavingSegment segment) {
        return null == segment ? Optional.empty() : new ExpressionConverter().convertToSQLNode(segment.getExpr());
    }
    
    @Override
    public Optional<HavingSegment> convertToSQLSegment(final SqlNode sqlNode) {
        return Optional.empty();
    }
}
