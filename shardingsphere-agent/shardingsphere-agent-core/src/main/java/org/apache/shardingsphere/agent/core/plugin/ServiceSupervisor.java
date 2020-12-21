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

package org.apache.shardingsphere.agent.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class ServiceSupervisor {
    
    private final List<Service> services;
    
    public ServiceSupervisor(final List<Service> services) {
        this.services = services;
    }
    
    
    /**
     * Initial all services.
     */
    public void setUpAllServices() {
        services.forEach(service -> {
            try {
                service.setup();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to initial service.", ex);
            }
        });
    }
    
    /**
     * Start all services.
     */
    public void startAllServices() {
        services.forEach(service -> {
            try {
                service.start();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to start service.", ex);
            }
        });
    }
    
    /**
     * Shutdown all services.
     */
    public void cleanUpAllServices() {
        services.forEach(service -> {
            try {
                service.cleanup();
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to shutdown service.", ex);
            }
        });
    }
    
}
