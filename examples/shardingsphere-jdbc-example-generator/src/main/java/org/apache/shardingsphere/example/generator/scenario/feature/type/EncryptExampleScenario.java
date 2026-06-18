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

package org.apache.shardingsphere.example.generator.scenario.feature.type;

import org.apache.shardingsphere.example.generator.scenario.feature.FeatureExampleScenario;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Encrypt example scenario.
 */
public final class EncryptExampleScenario implements FeatureExampleScenario {
    
    @Override
    public Map<String, String> getJavaClassTemplateMap() {
        Map<String, String> result = new HashMap<>();
        result.put("java/TestQueryAssistedShardingEncryptAlgorithm.ftl", "TestQueryAssistedShardingEncryptAlgorithm.java");
        return result;
    }
    
    @Override
    public Map<String, String> getResourceTemplateMap() {
        Map<String, String> result = new HashMap<>();
        result.put("resources/spi/encryptAlgorithm.ftl", "META-INF/services/org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm");
        return result;
    }
    
    @Override
    public Collection<String> getJavaClassPaths() {
        return Collections.emptySet();
    }
    
    @Override
    public Collection<String> getResourcePaths() {
        Collection<String> result = new HashSet<>();
        result.add("META-INF/services");
        return result;
    }
    
    @Override
    public String getType() {
        return "encrypt";
    }
}
