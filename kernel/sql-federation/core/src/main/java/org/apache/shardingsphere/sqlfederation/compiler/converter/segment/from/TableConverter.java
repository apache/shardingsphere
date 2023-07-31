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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.impl.DeleteMultiTableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.impl.JoinTableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.impl.SimpleTableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.impl.SubqueryTableConverter;

import java.util.Optional;

/**
 * Table converter.
 */
public final class TableConverter implements SQLSegmentConverter<TableSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final TableSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        if (segment instanceof SimpleTableSegment) {
            return new SimpleTableConverter().convert((SimpleTableSegment) segment);
        }
        if (segment instanceof JoinTableSegment) {
            return new JoinTableConverter().convert((JoinTableSegment) segment);
        }
        if (segment instanceof SubqueryTableSegment) {
            return new SubqueryTableConverter().convert((SubqueryTableSegment) segment);
        }
        if (segment instanceof DeleteMultiTableSegment) {
            return new DeleteMultiTableConverter().convert((DeleteMultiTableSegment) segment);
        }
        throw new UnsupportedSQLOperationException("Unsupported segment type: " + segment.getClass());
    }
}
