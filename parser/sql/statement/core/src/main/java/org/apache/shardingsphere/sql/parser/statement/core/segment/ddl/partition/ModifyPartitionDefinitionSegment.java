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

package org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.AlterDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Modify partition definition segment.
 */
@RequiredArgsConstructor
@Getter
public final class ModifyPartitionDefinitionSegment implements AlterDefinitionSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final Collection<PartitionSegment> partitions = new LinkedList<>();
    
    private final PropertiesSegment properties;
    
    private final boolean allPartitions;
    
    /**
     * Constructor for single partition.
     *
     * @param startIndex start index
     * @param stopIndex stop index
     * @param properties properties
     */
    public ModifyPartitionDefinitionSegment(final int startIndex, final int stopIndex, final PropertiesSegment properties) {
        this(startIndex, stopIndex, properties, false);
    }
}
