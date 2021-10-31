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

package org.apache.shardingsphere.infra.optimize.converter.segment.projection.impl;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Shorthand projection converter. 
 */
public final class ShorthandProjectionConverter implements SQLSegmentConverter<ShorthandProjectionSegment, SqlIdentifier> {
    
    @Override
    public Optional<SqlIdentifier> convertToSQLNode(final ShorthandProjectionSegment segment) {
        return null == segment ? Optional.empty() : Optional.of(SqlIdentifier.star(SqlParserPos.ZERO));
    }
    
    @Override
    public Optional<ShorthandProjectionSegment> convertToSQLSegment(final SqlIdentifier sqlIdentifier) {
        if (null == sqlIdentifier) {
            return Optional.empty();
        }
        ShorthandProjectionSegment result = new ShorthandProjectionSegment(getStartIndex(sqlIdentifier), getStopIndex(sqlIdentifier));
        if (sqlIdentifier.names.size() > 1) {
            SqlIdentifier owner = sqlIdentifier.getComponent(0);
            result.setOwner(new OwnerSegment(getStartIndex(owner), getStopIndex(owner), new IdentifierValue(sqlIdentifier.names.get(0))));
        }
        return Optional.of(result);
    }
}
