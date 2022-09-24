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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.core.exception.ShardingSphereAgentException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Agent path builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AgentPathBuilder {
    
    @Getter
    private static final File agentPath;
    
    @Getter
    private static final File pluginPath;
    
    static {
        agentPath = buildAgentPath();
        pluginPath = buildAgentPluginPath();
    }
    
    private static File buildAgentPath() {
        String classResourcePath = String.join("", AgentPathBuilder.class.getName().replaceAll("\\.", "/"), ".class");
        URL resource = ClassLoader.getSystemClassLoader().getResource(classResourcePath);
        if (null != resource) {
            String url = resource.toString();
            log.debug("The beacon class location is {}", url);
            int existFileInJarIndex = url.indexOf('!');
            boolean isInJar = existFileInJarIndex > -1;
            return isInJar ? getFileInJar(url, existFileInJarIndex) : getFileInResource(url, classResourcePath);
        }
        throw new ShardingSphereAgentException("Can not locate agent jar file.");
    }
    
    private static File getFileInResource(final String url, final String classResourcePath) {
        int prefixLength = "file:".length();
        String classLocation = url.substring(prefixLength, url.length() - classResourcePath.length());
        return new File(classLocation);
    }
    
    private static File getFileInJar(final String url, final int fileInJarIndex) {
        String realUrl = url.substring(url.indexOf("file:"), fileInJarIndex);
        try {
            File agentJarFile = new File(new URL(realUrl).toURI());
            return agentJarFile.exists() ? agentJarFile.getParentFile() : null;
        } catch (final MalformedURLException | URISyntaxException ex) {
            log.error(String.format("Can not locate agent jar file by url %s", url), ex);
            return null;
        }
    }
    
    private static File buildAgentPluginPath() {
        return new File(String.join("", agentPath.getPath(), "/plugins"));
    }
}
