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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.url.core.ShardingSphereURL;
import org.apache.shardingsphere.infra.url.core.arg.URLArgumentLineRender;
import org.apache.shardingsphere.infra.url.core.arg.URLArgumentPlaceholderTypeFactory;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereModeConfigurationURLLoader;
import org.apache.shardingsphere.infra.url.spi.ShardingSphereLocalFileURLLoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * ShardingSphere URL load engine.
 */
@RequiredArgsConstructor
public final class ShardingSphereURLLoadEngine {
    
    private final ShardingSphereURL url;
    
    /**
     * Load configuration content.
     *
     * @return loaded content
     * @throws ServiceProviderNotFoundException service provider not found exception
     */
    public Object loadContent() {
        Optional<ShardingSphereLocalFileURLLoader> localFileURLLoader = TypedSPILoader.findService(ShardingSphereLocalFileURLLoader.class, url.getSourceType());
        if (localFileURLLoader.isPresent()) {
            Collection<String> lines = Arrays.asList(localFileURLLoader.get().load(url.getConfigurationSubject(), url.getQueryProps()).split(System.lineSeparator()));
            return URLArgumentLineRender.render(lines, URLArgumentPlaceholderTypeFactory.valueOf(url.getQueryProps()));
        }
        Optional<ShardingSphereModeConfigurationURLLoader> modeConfigURLLoader = TypedSPILoader.findService(ShardingSphereModeConfigurationURLLoader.class, url.getSourceType());
        if (modeConfigURLLoader.isPresent()) {
            return modeConfigURLLoader.get().load(url.getConfigurationSubject(), url.getQueryProps());
        }
        throw new ServiceProviderNotFoundException(ShardingSphereModeConfigurationURLLoader.class, url.getSourceType());
    }
}
