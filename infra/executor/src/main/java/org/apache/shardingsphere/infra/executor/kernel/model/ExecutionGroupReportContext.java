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

package org.apache.shardingsphere.infra.executor.kernel.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.user.Grantee;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Execution group report context.
 */
@RequiredArgsConstructor
@Getter
public final class ExecutionGroupReportContext {
    
    // TODO processID should same with connectionId
    private final String processId;
    
    private final String databaseName;
    
    private final Grantee grantee;
    
    public ExecutionGroupReportContext(final String databaseName) {
        this(databaseName, new Grantee("", ""));
    }
    
    public ExecutionGroupReportContext(final String databaseName, final Grantee grantee) {
        this(new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", ""), databaseName, grantee);
    }
}
