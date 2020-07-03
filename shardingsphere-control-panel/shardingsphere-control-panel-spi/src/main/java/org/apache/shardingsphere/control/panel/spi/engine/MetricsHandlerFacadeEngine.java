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

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import org.apache.shardingsphere.control.panel.spi.metrics.MetricsHandlerFacade;

/**
 * Metrics handler facade engine.
 */
public final class MetricsHandlerFacadeEngine {
    
    private static MetricsHandlerFacade handlerFacade;
    
    static {
        handlerFacade = loadFirst();
    }
    
    /**
     * Build Metrics handler facade.
     *
     * @return Metrics handler facade.
     */
    public static Optional<MetricsHandlerFacade> build() {
        return Optional.ofNullable(handlerFacade);
    }
    
    private static MetricsHandlerFacade loadFirst() {
        final ServiceLoader<MetricsHandlerFacade> loader = loadAll();
        final Iterator<MetricsHandlerFacade> iterator = loader.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }
    
    private static ServiceLoader<MetricsHandlerFacade> loadAll() {
        return ServiceLoader.load(MetricsHandlerFacade.class);
    }
}

