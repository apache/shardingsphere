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

package org.apache.shardingsphere.scaling;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.spi.ScalingWorkerLoader;
import org.apache.shardingsphere.scaling.util.ServerConfigurationInitializer;


/**
 * Bootstrap of ShardingSphere-Scaling worker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ScalingWorkerBootstrap {
    
    /**
     * Worker Main entry.
     *
     * @param args running args
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        log.info("ShardingSphere-Scaling Worker Startup");
        ServerConfigurationInitializer.init();
        ScalingWorkerLoader.initScalingWorker();
        wait0();
    }
    
    private static synchronized void wait0() {
        try {
            ScalingWorkerBootstrap.class.wait();
        } catch (final InterruptedException ignored) {
        }
    }
}
