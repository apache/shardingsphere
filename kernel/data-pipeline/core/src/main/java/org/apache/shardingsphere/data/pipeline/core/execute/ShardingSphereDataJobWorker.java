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

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ShardingSphere data job worker.
 */
public final class ShardingSphereDataJobWorker {
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    /**
     * Initialize job worker.
     *
     * @param contextManager context manager
     */
    public static void initialize(final ContextManager contextManager) {
        if (WORKER_INITIALIZED.get()) {
            return;
        }
        synchronized (WORKER_INITIALIZED) {
            if (WORKER_INITIALIZED.get()) {
                return;
            }
            boolean collectorEnabled = contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_METADATA_COLLECTOR_ENABLED);
            if (collectorEnabled) {
                startScheduleThread(contextManager);
            }
            WORKER_INITIALIZED.set(true);
        }
    }
    
    private static void startScheduleThread(final ContextManager contextManager) {
        new ShardingSphereDataScheduleCollector(contextManager).start();
    }
}
