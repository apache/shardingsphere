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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlCollation;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;

import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;

import java.util.Optional;

public final class CollateExpressionConverter implements SQLSegmentConverter<CollateExpression, SqlNode> {

    @Override
    public Optional<SqlNode> convert(CollateExpression segment) {
        SqlCollation sqlCollation = new SqlCollation(SqlCollation.Coercibility.NONE);
        SqlIdentifier sqlIdentifier = new SqlIdentifier(segment.getTableName().getIdentifier().getValue(), sqlCollation, SqlParserPos.ZERO);
        return Optional.of(sqlIdentifier);
    }
}
