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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJobId;

/**
 * Migration job id.
 */
@Getter
@ToString(callSuper = true)
public final class MigrationJobId extends AbstractPipelineJobId {
    
    public static final String CURRENT_VERSION = "01";
    
    private final String sourceResourceName;
    
    private final String sourceSchemaName;
    
    private final String sourceTableName;
    
    private final String targetDatabaseName;
    
    private final String targetTableName;
    
    public MigrationJobId(@NonNull final String sourceResourceName, final String sourceSchemaName, @NonNull final String sourceTableName,
                          @NonNull final String targetDatabaseName, @NonNull final String targetTableName) {
        super(JobType.MIGRATION, CURRENT_VERSION);
        this.sourceSchemaName = sourceSchemaName;
        this.sourceResourceName = sourceResourceName;
        this.sourceTableName = sourceTableName;
        this.targetDatabaseName = targetDatabaseName;
        this.targetTableName = targetTableName;
    }
}
