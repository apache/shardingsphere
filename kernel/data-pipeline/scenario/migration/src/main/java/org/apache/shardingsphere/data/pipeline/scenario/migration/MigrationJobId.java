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
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.common.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Migration job id.
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public final class MigrationJobId implements PipelineJobId {
    
    @Getter
    private final PipelineJobType jobType = new MigrationJobType();
    
    @Getter
    private final PipelineContextKey contextKey;
    
    private final List<String> jobShardingDataNodes;
    
    @Override
    public String marshal() {
        return PipelineJobIdUtils.marshalPrefix(jobType, contextKey) + DigestUtils.md5Hex(JsonUtils.toJsonString(this).getBytes(StandardCharsets.UTF_8));
    }
}
