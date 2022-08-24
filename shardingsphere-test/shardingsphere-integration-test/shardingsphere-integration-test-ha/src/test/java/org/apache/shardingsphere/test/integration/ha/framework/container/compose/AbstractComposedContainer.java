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

package org.apache.shardingsphere.test.integration.ha.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.test.integration.env.container.atomic.ITContainers;
import org.testcontainers.lifecycle.Startable;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract composed container.
 */
public class AbstractComposedContainer implements Startable {
    
    @Getter
    private final ITContainers containers;
    
    private final String scenario;
    
    public AbstractComposedContainer(String scenario) {
        this.scenario = scenario;
        this.containers = new ITContainers(scenario);
    }
    
    @Override
    public void start() {
        getContainers().start();
    }
    
    @Override
    public void stop() {
        getContainers().stop();
    }
    
    public final Map<String, String> loadContainerNames() {
        Map<String, String> result = new HashMap<>(3, 1);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("env/" + scenario + "/");
        if (resource != null) {
            String[] containerNames = new File(resource.getPath()).list((dir, name) -> new File(dir, name).isDirectory());
            if (containerNames != null) {
                
            }
        }
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        AbstractComposedContainer abstractComposedContainer = new AbstractComposedContainer("mysql/mysql-ha");
        abstractComposedContainer.loadContainerNames();
    }
}
