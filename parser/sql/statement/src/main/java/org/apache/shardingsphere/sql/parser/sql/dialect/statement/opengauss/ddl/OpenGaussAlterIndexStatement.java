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

package org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl;

import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;

import java.util.Optional;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.MovePartitionSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.segment.RenamePartitionSegment;

/**
 * OpenGauss alter index statement.
 */
@Setter
public final class OpenGaussAlterIndexStatement extends AlterIndexStatement implements OpenGaussStatement {
    
    private IndexSegment renameIndex;
    
    private MovePartitionSegment movePartition;
    
    private RenamePartitionSegment renamePartition;
    
    /**
     * Get rename index segment.
     *
     * @return rename index segment
     */
    public Optional<IndexSegment> getRenameIndex() {
        return Optional.ofNullable(renameIndex);
    }
    
    /**
     * Get move partition segment.
     *
     * @return move partition segment
     */
    public Optional<MovePartitionSegment> getMovePartition() {
        return Optional.ofNullable(movePartition);
    }
    
    /**
     * Get rename partition segment.
     *
     * @return rename partition segment
     */
    public Optional<RenamePartitionSegment> getRenamePartition() {
        return Optional.ofNullable(renamePartition);
    }
}
