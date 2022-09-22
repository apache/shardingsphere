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

package org.apache.shardingsphere.data.pipeline.core.execute;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNodeWatcher;

/**
 * Pipeline job worker.
 */
@Slf4j
public final class PipelineJobWorker {
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    /**
     * Initialize job worker.
     */
    public static void initialize() {
        if (WORKER_INITIALIZED.get()) {
            return;
        }
        synchronized (WORKER_INITIALIZED) {
            if (WORKER_INITIALIZED.get()) {
                return;
            }
            log.info("start worker initialization");
            PipelineMetaDataNodeWatcher.getInstance();
            WORKER_INITIALIZED.set(true);
            log.info("worker initialization done");
        }
    }
}
