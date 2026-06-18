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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * Consistency check job item info.
 */
// TODO use final for fields
// TODO embed ConsistencyCheckJobItemProgress to reduce fields
@Getter
@Setter
public final class ConsistencyCheckJobItemInfo {
    
    private boolean active;
    
    private String tableNames;
    
    private Boolean checkSuccess;
    
    private String checkFailedTableNames;
    
    private String ignoredTableNames;
    
    private int inventoryFinishedPercentage;
    
    private long inventoryRemainingSeconds;
    
    private Long incrementalIdleSeconds;
    
    private String checkBeginTime;
    
    private String checkEndTime;
    
    private long durationSeconds;
    
    private String algorithmType;
    
    private String algorithmProps;
    
    private String errorMessage;
}
