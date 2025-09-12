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

package org.apache.shardingsphere.data.pipeline.core.task;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCancelingException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Pipeline task utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineTaskUtils {
    
    /**
     * Generate inventory task ID.
     *
     * @param dumperContext inventory dumper context
     * @return generated ID
     */
    public static String generateInventoryTaskId(final InventoryDumperContext dumperContext) {
        return String.format("%s.%s#%s", dumperContext.getCommonContext().getDataSourceName(), dumperContext.getActualTableName(), dumperContext.getShardingItem());
    }
    
    /**
     * Create incremental task progress.
     *
     * @param position ingest position
     * @param initProgress initial job item progress
     * @return incremental task progress
     */
    public static IncrementalTaskProgress createIncrementalTaskProgress(final IngestPosition position, final TransmissionJobItemProgress initProgress) {
        IncrementalTaskProgress result = new IncrementalTaskProgress(position);
        if (null != initProgress && null != initProgress.getIncremental()) {
            Optional.ofNullable(initProgress.getIncremental().getIncrementalTaskProgress())
                    .ifPresent(optional -> result.setIncrementalTaskDelay(initProgress.getIncremental().getIncrementalTaskProgress().getIncrementalTaskDelay()));
        }
        return result;
    }
    
    /**
     * Wait future.
     *
     * @param future future to wait
     * @param <T> result type
     * @return execution result
     * @throws SQLWrapperException if the future execution fails
     */
    public static <T> T waitFuture(final Future<T> future) {
        try {
            return future.get();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLWrapperException(new SQLException(ex));
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof PipelineSQLException || ex.getCause() instanceof PipelineJobCancelingException) {
                throw (PipelineSQLException) ex.getCause();
            }
            throw new SQLWrapperException(new SQLException(ex));
        }
    }
}
