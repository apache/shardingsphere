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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider;

import java.nio.charset.StandardCharsets;

/**
 * Apollo driver URL provider.
 */
public final class ApolloDriverURLProvider implements ShardingSphereDriverURLProvider {
    
    private static final String APOLLO_TYPE = "apollo:";
    
    @Override
    public boolean accept(final String url) {
        return StringUtils.isNotBlank(url) && url.contains(APOLLO_TYPE);
    }
    
    @Override
    public byte[] getContent(final String url, final String urlPrefix) {
        String configPath = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String namespace = configPath.substring(APOLLO_TYPE.length());
        Preconditions.checkArgument(!namespace.isEmpty(), "Apollo namespace is required in ShardingSphere driver URL.");
        ConfigFile configFile = ConfigService.getConfigFile(namespace, ConfigFileFormat.YAML);
        return configFile.getContent().getBytes(StandardCharsets.UTF_8);
    }
}
