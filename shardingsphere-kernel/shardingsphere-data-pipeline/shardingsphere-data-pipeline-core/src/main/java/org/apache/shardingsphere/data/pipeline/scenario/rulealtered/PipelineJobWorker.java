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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.execute.FinishedCheckJobExecutor;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineJobExecutor;

/**
 * Pipeline job worker.
 */
@Slf4j
public final class PipelineJobWorker {
    
    private static final RuleAlteredJobWorker INSTANCE = new RuleAlteredJobWorker();
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    /**
     * Initialize job worker if necessary.
     */
    public static void initWorkerIfNecessary() {
        if (WORKER_INITIALIZED.get()) {
            return;
        }
        synchronized (WORKER_INITIALIZED) {
            if (WORKER_INITIALIZED.get()) {
                return;
            }
            log.info("start worker initialization");
            PipelineContext.getContextManager().getInstanceContext().getEventBusContext().register(INSTANCE);
            new FinishedCheckJobExecutor().start();
            new PipelineJobExecutor().start();
            WORKER_INITIALIZED.set(true);
            log.info("worker initialization done");
        }
    }
}
