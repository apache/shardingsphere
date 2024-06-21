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

package org.apache.shardingsphere.sql.parser.statement.doris.ddl;

import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.DorisStatement;

import java.util.Optional;

/**
 * Doris drop index statement.
 */
@Setter
public final class DorisDropIndexStatement extends DropIndexStatement implements DorisStatement {
    
    private SimpleTableSegment simpleTable;
    
    private AlgorithmTypeSegment algorithmType;
    
    private LockTableSegment lockTable;
    
    @Override
    public Optional<SimpleTableSegment> getSimpleTable() {
        return Optional.ofNullable(simpleTable);
    }
    
    @Override
    public Optional<AlgorithmTypeSegment> getAlgorithmType() {
        return Optional.ofNullable(algorithmType);
    }
    
    @Override
    public Optional<LockTableSegment> getLockTable() {
        return Optional.ofNullable(lockTable);
    }
}
