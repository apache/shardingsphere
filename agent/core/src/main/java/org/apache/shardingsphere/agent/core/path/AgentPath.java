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

package org.apache.shardingsphere.agent.core.path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.preconditions.AgentPreconditions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * Agent path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentPath {
    
    /**
     * Get agent root path.
     * 
     * @return agent root path
     */
    public static File getRootPath() {
        String classResourcePath = String.join("", AgentPath.class.getName().replaceAll("\\.", "/"), ".class");
        URL resource = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(classResourcePath), "Can not locate agent jar file.");
        return getJarFile(resource.toString()).getParentFile();
    }
    
    private static File getJarFile(final String url) {
        try {
            File result = new File(new URL(url.substring(url.indexOf("file:"), url.indexOf('!'))).toURI());
            AgentPreconditions.checkState(result.exists(), String.format("Can not locate agent jar file by URL `%s`.", url));
            return result;
        } catch (final MalformedURLException | URISyntaxException ex) {
            throw new IllegalStateException(String.format("Can not locate agent jar file by URL `%s`.", url), ex);
        }
    }
}
