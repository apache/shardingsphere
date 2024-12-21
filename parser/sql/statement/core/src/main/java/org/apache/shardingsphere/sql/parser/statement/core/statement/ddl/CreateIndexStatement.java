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

package org.apache.shardingsphere.sql.parser.statement.core.statement.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Create index statement.
 */
@Getter
@Setter
public abstract class CreateIndexStatement extends AbstractSQLStatement implements DDLStatement {
    
    private IndexSegment index;
    
    private SimpleTableSegment table;
    
    private final Collection<ColumnSegment> columns = new LinkedList<>();
    
    /**
     * Get generated index start index.
     *
     * @return generated index start index
     */
    public Optional<Integer> getGeneratedIndexStartIndex() {
        return Optional.empty();
    }
    
    /**
     * Set generated index start index.
     *
     * @param generatedIndexStartIndex generated index start index
     */
    public void setGeneratedIndexStartIndex(final Integer generatedIndexStartIndex) {
    }
    
    /**
     * Judge whether contains if not exists or not.
     *
     * @return whether contains if not exists or not
     */
    public boolean isIfNotExists() {
        return false;
    }
    
    /**
     * Set if not exists or not.
     *
     * @param ifNotExists if not exists or not
     */
    public void setIfNotExists(final boolean ifNotExists) {
    }
    
    /**
     * Get algorithm type.
     *
     * @return algorithm type
     */
    public Optional<AlgorithmTypeSegment> getAlgorithmType() {
        return Optional.empty();
    }
    
    /**
     * Set algorithm type.
     *
     * @param algorithmType algorithm type
     */
    public void setAlgorithmType(final AlgorithmTypeSegment algorithmType) {
    }
    
    /**
     * Get lock table.
     *
     * @return lock table
     */
    public Optional<LockTableSegment> getLockTable() {
        return Optional.empty();
    }
    
    /**
     * Set lock table.
     *
     * @param lockTable lock table
     */
    public void setLockTable(final LockTableSegment lockTable) {
    }
}
