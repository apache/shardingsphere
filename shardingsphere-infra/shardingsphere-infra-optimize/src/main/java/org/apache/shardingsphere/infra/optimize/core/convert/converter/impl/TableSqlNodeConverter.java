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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Optional;

/**
 * convert from clause.
 */
public final class TableSqlNodeConverter implements SqlNodeConverter<TableSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final TableSegment table) {
        if (table instanceof SimpleTableSegment) {
            return new SimpleTableSqlNodeConverter().convert((SimpleTableSegment) table);
        } else if (table instanceof JoinTableSegment) {
            return new JoinTableSqlNodeConverter().convert((JoinTableSegment) table);
        } else if (table instanceof SubqueryTableSegment) {
            return new SubqueryTableSqlNodeConverter().convert((SubqueryTableSegment) table);
        }
        throw new UnsupportedOperationException("unsupportd TableSegment type: " + table.getClass());
    }
}
