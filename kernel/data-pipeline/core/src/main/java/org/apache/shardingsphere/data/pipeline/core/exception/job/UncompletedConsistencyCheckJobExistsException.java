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

package org.apache.shardingsphere.data.pipeline.core.exception.job;

import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Uncompleted consistency check job exists exception.
 */
public final class UncompletedConsistencyCheckJobExistsException extends PipelineJobException {
    
    private static final long serialVersionUID = 2854259384634892428L;
    
    public UncompletedConsistencyCheckJobExistsException(final String jobId, final ConsistencyCheckJobItemProgress progress) {
        super(XOpenSQLState.GENERAL_ERROR, 13, String.format("Uncompleted consistency check job '%s' exists, progress '%s'.", jobId, progress));
    }
}
