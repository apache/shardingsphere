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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml;

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.WithSQLStatementAttribute;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Merge statement.
 */
@Getter
public final class MergeStatement extends DMLStatement {
    
    private final TableSegment target;
    
    private final TableSegment source;
    
    private final ExpressionWithParamsSegment expression;
    
    private final UpdateStatement update;
    
    private final InsertStatement insert;
    
    private final WithSegment with;
    
    private final WithTableHintSegment withTableHint;
    
    private final Collection<IndexSegment> indexes;
    
    private final OutputSegment output;
    
    private final OptionHintSegment optionHint;
    
    private final Collection<MergeWhenAndThenSegment> whenAndThens;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private MergeStatement(final DatabaseType databaseType, final TableSegment target, final TableSegment source, final ExpressionWithParamsSegment expression,
                           final UpdateStatement update, final InsertStatement insert, final WithSegment with, final WithTableHintSegment withTableHint,
                           final Collection<IndexSegment> indexes, final OutputSegment output, final OptionHintSegment optionHint,
                           final Collection<MergeWhenAndThenSegment> whenAndThens) {
        super(databaseType);
        this.target = target;
        this.source = source;
        this.expression = expression;
        this.update = update;
        this.insert = insert;
        this.with = with;
        this.withTableHint = withTableHint;
        this.indexes = null == indexes ? new LinkedList<>() : indexes;
        this.output = output;
        this.optionHint = optionHint;
        this.whenAndThens = null == whenAndThens ? new LinkedList<>() : whenAndThens;
        attributes = new SQLStatementAttributes(new WithSQLStatementAttribute(with));
    }
    
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
     * Get with.
     *
     * @return with
     */
    public Optional<WithSegment> getWith() {
        return Optional.ofNullable(with);
    }
    
    /**
     * Get with table hint.
     *
     * @return with table hint
     */
    public Optional<WithTableHintSegment> getWithTableHint() {
        return Optional.ofNullable(withTableHint);
    }
    
    /**
     * Get output.
     *
     * @return output
     */
    public Optional<OutputSegment> getOutput() {
        return Optional.ofNullable(output);
    }
    
    /**
     * Get option hint.
     *
     * @return option hint.
     */
    public Optional<OptionHintSegment> getOptionHint() {
        return Optional.ofNullable(optionHint);
    }
}
