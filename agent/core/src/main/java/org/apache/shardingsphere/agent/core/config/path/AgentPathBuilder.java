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

package org.apache.shardingsphere.agent.core.config.path;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * Agent path builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentPathBuilder {
    
    @Getter
    private static File agentPath;
    
    @Getter
    private static File pluginPath;
    
    static {
        agentPath = buildAgentPath();
        pluginPath = buildAgentPluginPath();
    }
    
    private static File buildAgentPath() {
        String classResourcePath = String.join("", AgentPathBuilder.class.getName().replaceAll("\\.", "/"), ".class");
        URL resource = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(classResourcePath), "Can not locate agent jar file");
        String url = resource.toString();
        int existFileInJarIndex = url.indexOf('!');
        boolean isInJar = existFileInJarIndex > -1;
        return isInJar ? getFileInJar(url, existFileInJarIndex) : getFileInResource(url, classResourcePath);
    }
    
    private static File getFileInJar(final String url, final int fileInJarIndex) {
        String realUrl = url.substring(url.indexOf("file:"), fileInJarIndex);
        try {
            File agentJarFile = new File(new URL(realUrl).toURI());
            Preconditions.checkState(agentJarFile.exists(), "Can not locate agent jar file by URL `%s`", url);
            return agentJarFile.getParentFile();
        } catch (final MalformedURLException | URISyntaxException ex) {
            throw new IllegalStateException(String.format("Can not locate agent jar file by URL `%s`", url));
        }
    }
    
    private static File getFileInResource(final String url, final String classResourcePath) {
        int prefixLength = "file:".length();
        String classLocation = url.substring(prefixLength, url.length() - classResourcePath.length());
        return new File(classLocation);
    }
    
    private static File buildAgentPluginPath() {
        return new File(String.join("/", agentPath.getPath(), "plugins"));
    }
}
