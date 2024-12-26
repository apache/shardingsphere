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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Drop index statement.
 */
@Getter
public abstract class DropIndexStatement extends AbstractSQLStatement implements DDLStatement {
    
    private final Collection<IndexSegment> indexes = new LinkedList<>();
    
    /**
     * Get simple table.
     *
     * @return simple table
     */
    public Optional<SimpleTableSegment> getSimpleTable() {
        return Optional.empty();
    }
    
    /**
     * Set simple table.
     *
     * @param simpleTableSegment simple table
     */
    public void setSimpleTable(final SimpleTableSegment simpleTableSegment) {
    }
    
    /**
     * Judge whether contains exist clause or not.
     *
     * @return whether contains exist clause or not
     */
    public boolean isIfExists() {
        return false;
    }
    
    /**
     * Set if exists or not.
     *
     * @param ifExists if exists or not
     */
    public void setIfExists(final boolean ifExists) {
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
     * @param algorithmTypeSegment algorithm type
     */
    public void setAlgorithmType(final AlgorithmTypeSegment algorithmTypeSegment) {
    }
    
    /**
     * Get lock table segment.
     *
     * @return lock table segment
     */
    public Optional<LockTableSegment> getLockTable() {
        return Optional.empty();
    }
    
    /**
     * Set lock table segment.
     *
     * @param lockTableSegment lock table segment
     */
    public void setLockTable(final LockTableSegment lockTableSegment) {
    }
}
