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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi.classpath;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider;

import java.io.InputStream;

/**
 * Abstract classpath URL provider.
 */
public abstract class AbstractClasspathURLProvider implements ShardingSphereURLProvider {
    
    String getConfigurationFile(final String url, final String urlPrefix, final String pathType) {
        String configuredFile = url.substring(urlPrefix.length(), url.contains("?") ? url.indexOf('?') : url.length());
        String file = configuredFile.substring(pathType.length());
        Preconditions.checkArgument(!file.isEmpty(), "Configuration file is required in ShardingSphere URL.");
        return file;
    }
    
    InputStream getResourceAsStream(final String resource) {
        InputStream result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        result = null == result ? Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + resource) : result;
        if (null != result) {
            return result;
        }
        throw new IllegalArgumentException(String.format("Can not find configuration file `%s`.", resource));
    }
}
