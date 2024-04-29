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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerMergeStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Merge statement helper class for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergeStatementHandler implements SQLStatementHandler {
    
    /**
     * Get with segment.
     *
     * @param mergeStatement merge statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final MergeStatement mergeStatement) {
        if (mergeStatement instanceof SQLServerMergeStatement) {
            return ((SQLServerMergeStatement) mergeStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get with table hint segment.
     *
     * @param mergeStatement merge statement
     * @return with table hint segment
     */
    public static Optional<WithTableHintSegment> getWithTableHintSegment(final MergeStatement mergeStatement) {
        if (mergeStatement instanceof SQLServerMergeStatement) {
            return ((SQLServerMergeStatement) mergeStatement).getWithTableHintSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get output segment.
     *
     * @param mergeStatement merge statement
     * @return output segment
     */
    public static Optional<OutputSegment> getOutputSegment(final MergeStatement mergeStatement) {
        if (mergeStatement instanceof SQLServerMergeStatement) {
            return ((SQLServerMergeStatement) mergeStatement).getOutputSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get when and then segments.
     *
     * @param mergeStatement merge statement
     * @return when and then segments
     */
    public static Collection<MergeWhenAndThenSegment> getWhenAndThenSegments(final MergeStatement mergeStatement) {
        if (mergeStatement instanceof SQLServerMergeStatement) {
            return ((SQLServerMergeStatement) mergeStatement).getWhenAndThenSegments();
        }
        return Collections.emptyList();
    }
    
    /**
     * Get index segments.
     *
     * @param mergeStatement merge statement
     * @return index segments
     */
    public static Collection<IndexSegment> getIndexes(final MergeStatement mergeStatement) {
        if (mergeStatement instanceof SQLServerMergeStatement) {
            return ((SQLServerMergeStatement) mergeStatement).getIndexes();
        }
        return Collections.emptyList();
    }
    
}
