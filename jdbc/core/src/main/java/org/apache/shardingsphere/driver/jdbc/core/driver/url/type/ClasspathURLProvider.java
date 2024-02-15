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

package org.apache.shardingsphere.driver.jdbc.core.driver.url.type;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.ShardingSphereURL;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.ShardingSphereURLProvider;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.URLArgumentPlaceholderTypeFactory;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.reader.ConfigurationContentReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classpath URL provider.
 */
public final class ClasspathURLProvider implements ShardingSphereURLProvider {
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final ShardingSphereURL url) {
        try (InputStream inputStream = getResourceAsStreamFromClasspath(url.getConfigurationSubject())) {
            return ConfigurationContentReader.read(inputStream, URLArgumentPlaceholderTypeFactory.valueOf(url.getParameters()));
        }
    }
    
    private InputStream getResourceAsStreamFromClasspath(final String resource) {
        InputStream result = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        result = null == result ? Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + resource) : result;
        Preconditions.checkNotNull(result, "Can not find configuration file `%s`.", resource);
        return result;
    }
    
    @Override
    public String getType() {
        return "classpath:";
    }
}
