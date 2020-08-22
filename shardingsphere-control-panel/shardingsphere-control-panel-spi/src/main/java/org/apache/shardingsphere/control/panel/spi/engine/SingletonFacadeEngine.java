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

package org.apache.shardingsphere.control.panel.spi.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.control.panel.spi.metrics.MetricsHandlerFacade;
import org.apache.shardingsphere.infra.spi.singleton.SingletonServiceLoader;

import java.util.Optional;

/**
 * Singleton facade engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingletonFacadeEngine {
    
    /**
     * Build metrics handler facade.
     *
     * @return Metrics handler facade.
     */
    public static Optional<MetricsHandlerFacade> buildMetrics() {
        return SingletonServiceLoader.getServiceLoader(MetricsHandlerFacade.class).newServiceInstances();
    }
}
