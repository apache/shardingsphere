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

package org.apache.shardingsphere.agent.core.plugin.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory.Logger;
import org.apache.shardingsphere.agent.core.plugin.yaml.path.AgentPathBuilder;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Agent plugin loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentPluginLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentPluginLoader.class);
    
    /**
     * Load plugin jars.
     * 
     * @return plugin jars
     * @throws IOException IO exception
     */
    public static Collection<PluginJar> load() throws IOException {
        List<File> jarFiles = new LinkedList<>();
        AgentPathBuilder.getPluginClassPaths().forEach(each -> jarFiles.addAll(collectJarFiles(each)));
        if (jarFiles.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<PluginJar> result = new LinkedList<>();
        for (File each : jarFiles) {
            result.add(new PluginJar(new JarFile(each, true), each));
            LOGGER.info("Loaded jar: {}", each.getName());
        }
        return result;
    }
    
    private static List<File> collectJarFiles(final File path) {
        File[] jarFiles = path.listFiles(each -> each.getName().endsWith(".jar"));
        return (null == jarFiles || jarFiles.length == 0) ? Collections.emptyList() : Arrays.asList(jarFiles);
    }
}
