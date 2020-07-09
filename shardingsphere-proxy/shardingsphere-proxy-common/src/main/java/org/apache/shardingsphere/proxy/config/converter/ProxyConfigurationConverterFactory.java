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

package org.apache.shardingsphere.proxy.config.converter;

import java.util.Optional;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.singleton.SingletonServiceLoader;

/**
 * Proxy configuration converter factory.
 */
public final class ProxyConfigurationConverterFactory {
    
    /**
     * New proxy configuration converter instances.
     *
     * @param isOrchestration is orchestration
     * @return proxy configuration converter
     */
    public static ProxyConfigurationConverter newInstances(final boolean isOrchestration) {
        if (isOrchestration) {
            return loadConverter();
        } else {
            return new DefaultConfigurationConverter();
        }
    }
    
    private static ProxyConfigurationConverter loadConverter() {
        Optional<ProxyConfigurationConverter> configurationConverter = SingletonServiceLoader.getServiceLoader(ProxyConfigurationConverter.class).newServiceInstances();
        if (!configurationConverter.isPresent()) {
            throw new ServiceProviderNotFoundException(ProxyConfigurationConverter.class);
        }
        return configurationConverter.get();
    }
}
