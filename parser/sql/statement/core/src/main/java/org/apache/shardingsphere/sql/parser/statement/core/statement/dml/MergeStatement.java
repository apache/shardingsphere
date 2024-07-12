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

package org.apache.shardingsphere.sql.parser.statement.core.statement.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Merge statement.
 */
@Getter
@Setter
public abstract class MergeStatement extends AbstractSQLStatement implements DMLStatement {
    
    private TableSegment target;
    
    private TableSegment source;
    
    private ExpressionWithParamsSegment expression;
    
    private UpdateStatement update;
    
    private InsertStatement insert;
    
    /**
     * Get update statement.
     *
     * @return update statement
     */
    public Optional<UpdateStatement> getUpdate() {
        return Optional.ofNullable(update);
    }
    
    /**
     * Get insert statement.
     *
     * @return insert statement
     */
    public Optional<InsertStatement> getInsert() {
        return Optional.ofNullable(insert);
    }
    
    /**
     * Get with segment.
     *
     * @return with segment
     */
    public Optional<WithSegment> getWithSegment() {
        return Optional.empty();
    }
    
    /**
     * Get with table hint segment.
     *
     * @return with table hint segment
     */
    public Optional<WithTableHintSegment> getWithTableHintSegment() {
        return Optional.empty();
    }
    
    /**
     * Get output segment.
     *
     * @return output segment
     */
    public Optional<OutputSegment> getOutputSegment() {
        return Optional.empty();
    }
    
    /**
     * Get when and then segments.
     *
     * @return when and then segments
     */
    public Collection<MergeWhenAndThenSegment> getWhenAndThenSegments() {
        return Collections.emptyList();
    }
    
    /**
     * Get index segments.
     *
     * @return index segments
     */
    public Collection<IndexSegment> getIndexes() {
        return Collections.emptyList();
    }
}
