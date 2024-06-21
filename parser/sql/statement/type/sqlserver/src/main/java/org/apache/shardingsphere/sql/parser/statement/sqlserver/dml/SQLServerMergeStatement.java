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

package org.apache.shardingsphere.sql.parser.statement.sqlserver.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.SQLServerStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * SQLServer merge statement.
 */
@Setter
@Getter
public final class SQLServerMergeStatement extends MergeStatement implements SQLServerStatement {
    
    private WithSegment withSegment;
    
    private WithTableHintSegment withTableHintSegment;
    
    private Collection<IndexSegment> indexes = new LinkedList<>();
    
    private OutputSegment outputSegment;
    
    private OptionHintSegment optionHintSegment;
    
    private Collection<MergeWhenAndThenSegment> whenAndThenSegments = new LinkedList<>();
    
    @Override
    public Optional<WithSegment> getWithSegment() {
        return Optional.ofNullable(withSegment);
    }
    
    @Override
    public Optional<WithTableHintSegment> getWithTableHintSegment() {
        return Optional.ofNullable(withTableHintSegment);
    }
    
    @Override
    public Optional<OutputSegment> getOutputSegment() {
        return Optional.ofNullable(outputSegment);
    }
    
    /**
     * Get option hint segment.
     *
     * @return option hint segment.
     */
    public Optional<OptionHintSegment> getOptionHintSegment() {
        return Optional.ofNullable(optionHintSegment);
    }
}
