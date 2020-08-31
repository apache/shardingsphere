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

package org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.CreateDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Constraint definition segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class ConstraintDefinitionSegment implements CreateDefinitionSegment, AlterDefinitionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final Collection<ColumnSegment> primaryKeyColumns = new LinkedList<>();
    
    private final Collection<ColumnSegment> indexColumns = new LinkedList<>();
    
    private IndexSegment indexName;
    
    private SimpleTableSegment referencedTable;
    
    /**
     * Get referenced table.
     * 
     * @return referenced table
     */
    public Optional<SimpleTableSegment> getReferencedTable() {
        return Optional.ofNullable(referencedTable);
    }
}
