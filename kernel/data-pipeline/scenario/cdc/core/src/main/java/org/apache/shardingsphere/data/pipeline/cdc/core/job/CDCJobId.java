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

package org.apache.shardingsphere.data.pipeline.cdc.core.job;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJobId;

import java.util.List;

/**
 * CDC job id.
 */
@Getter
@ToString(callSuper = true)
public final class CDCJobId extends AbstractPipelineJobId {
    
    private final List<String> schemaTableNames;
    
    private final boolean full;
    
    private final String sinkType;
    
    public CDCJobId(final PipelineContextKey contextKey, final List<String> schemaTableNames, final boolean full, final String sinkType) {
        super(new CDCJobType(), contextKey);
        this.schemaTableNames = schemaTableNames;
        this.full = full;
        this.sinkType = sinkType;
    }
}
