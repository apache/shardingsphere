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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Batch completion result for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdBatchCompletion {
    
    public static final int EXECUTE_FAILED = -1;
    
    private final int recordsCount;
    
    private final int[] updateCounts;
    
    private final Collection<Failure> failures;
    
    public FirebirdBatchCompletion(final int recordsCount, final int[] updateCounts) {
        this(recordsCount, updateCounts, Collections.emptyList());
    }
    
    /**
     * Failure of a batch message.
     */
    @RequiredArgsConstructor
    @Getter
    public static final class Failure {
        
        private final int messageIndex;
        
        private final SQLException cause;
    }
}
