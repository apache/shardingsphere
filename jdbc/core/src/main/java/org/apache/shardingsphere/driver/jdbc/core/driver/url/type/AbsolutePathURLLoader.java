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

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.ShardingSphereURL;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.ShardingSphereURLLoader;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.URLArgumentPlaceholderTypeFactory;
import org.apache.shardingsphere.driver.jdbc.core.driver.url.arg.URLArgumentLineRender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Absolute path URL loader.
 */
public final class AbsolutePathURLLoader implements ShardingSphereURLLoader {
    
    @Override
    @SneakyThrows(IOException.class)
    public byte[] getContent(final ShardingSphereURL url) {
        Path path = getAbsoluteFile(url.getConfigurationSubject()).toPath();
        return URLArgumentLineRender.render(readLines(path), URLArgumentPlaceholderTypeFactory.valueOf(url.getParameters()));
    }
    
    private File getAbsoluteFile(final String configurationSubject) {
        return new File(configurationSubject);
    }
    
    private Collection<String> readLines(final Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8).stream().filter(each -> !each.startsWith("#")).collect(Collectors.toList());
    }
    
    @Override
    public String getType() {
        return "absolutepath:";
    }
}
