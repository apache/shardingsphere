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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.position;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;

/**
 * Table check range position.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public final class TableCheckRangePosition {
    
    private final Integer splittingItem;
    
    private final String sourceDataNode;
    
    private final String logicTableName;
    
    private final PrimaryKeyIngestPosition<?> sourceRange;
    
    private final PrimaryKeyIngestPosition<?> targetRange;
    
    private final String queryCondition;
    
    private volatile Object sourcePosition;
    
    private volatile Object targetPosition;
    
    private volatile boolean finished;
    
    private volatile Boolean matched;
}
