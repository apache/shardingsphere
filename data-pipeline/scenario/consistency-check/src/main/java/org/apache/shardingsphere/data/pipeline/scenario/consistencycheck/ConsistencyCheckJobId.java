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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util.ConsistencyCheckSequence;

/**
 * Consistency check job id.
 */
@Getter
public final class ConsistencyCheckJobId implements PipelineJobId {
    
    private final ConsistencyCheckJobType jobType = new ConsistencyCheckJobType();
    
    private final PipelineContextKey contextKey;
    
    private final String parentJobId;
    
    private final int sequence;
    
    public ConsistencyCheckJobId(final PipelineContextKey contextKey, final String parentJobId) {
        this(contextKey, parentJobId, ConsistencyCheckSequence.MIN_SEQUENCE);
    }
    
    public ConsistencyCheckJobId(final PipelineContextKey contextKey, final String parentJobId, final String latestCheckJobId) {
        this(contextKey, parentJobId, ConsistencyCheckSequence.getNextSequence(parseSequence(latestCheckJobId)));
    }
    
    public ConsistencyCheckJobId(final PipelineContextKey contextKey, final String parentJobId, final int sequence) {
        this.contextKey = contextKey;
        this.parentJobId = parentJobId;
        this.sequence = sequence > ConsistencyCheckSequence.MAX_SEQUENCE ? ConsistencyCheckSequence.MIN_SEQUENCE : sequence;
    }
    
    /**
     * Parse consistency check sequence.
     *
     * @param checkJobId consistency check job id
     * @return sequence
     */
    public static int parseSequence(final String checkJobId) {
        return Integer.parseInt(checkJobId.substring(checkJobId.length() - 1));
    }
    
    @Override
    public String marshalSuffix() {
        return parentJobId + sequence;
    }
}
