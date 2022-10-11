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

package org.apache.shardingsphere.agent.plugin.logging.base.definition;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.spi.definition.AbstractPluginDefinitionService;

/**
 * Base logging plugin definition service.
 */
public final class BaseLoggingPluginDefinitionService extends AbstractPluginDefinitionService {
    
    private static final String SCHEMA_METADATA_LOADER_CLASS = "org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory";
    
    private static final String SCHEMA_METADATA_LOADER_METHOD_NAME = "create";
    
    private static final String SCHEMA_METADATA_LOADER_ADVICE_CLASS = "org.apache.shardingsphere.agent.plugin.logging.base.advice.MetaDataContextsFactoryAdvice";
    
    @Override
    public void defineInterceptors() {
        defineInterceptor(SCHEMA_METADATA_LOADER_CLASS)
                .aroundClassStaticMethod(ElementMatchers.named(SCHEMA_METADATA_LOADER_METHOD_NAME))
                .implement(SCHEMA_METADATA_LOADER_ADVICE_CLASS)
                .build();
    }
    
    @Override
    public String getType() {
        return "BaseLogging";
    }
}
