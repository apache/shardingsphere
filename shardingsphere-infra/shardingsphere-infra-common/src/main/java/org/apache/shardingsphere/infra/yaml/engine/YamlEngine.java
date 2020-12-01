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

package org.apache.shardingsphere.infra.yaml.engine;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.yaml.config.YamlConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.constructor.ClassFilterConstructor;
import org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstructor;
import org.apache.shardingsphere.infra.yaml.engine.representer.ShardingSphereYamlRepresenter;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * YAML engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlEngine {
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlFile YAML file
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     * @throws IOException IO Exception
     */
    public static <T extends YamlConfiguration> T unmarshal(final File yamlFile, final Class<T> classType) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)
        ) {
            return new Yaml(new ShardingSphereYamlConstructor(classType)).loadAs(inputStreamReader, classType);
        }
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlBytes YAML bytes
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     * @throws IOException IO Exception
     */
    public static <T extends YamlConfiguration> T unmarshal(final byte[] yamlBytes, final Class<T> classType) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(yamlBytes)) {
            return new Yaml(new ShardingSphereYamlConstructor(classType)).loadAs(inputStream, classType);
        }
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlContent YAML content
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     */
    public static <T> T unmarshal(final String yamlContent, final Class<T> classType) {
        return new Yaml(new ShardingSphereYamlConstructor(classType)).loadAs(yamlContent, classType);
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlContent YAML content
     * @param acceptClasses accept classes
     * @return map from YAML
     */
    public static Map<?, ?> unmarshal(final String yamlContent, final Collection<Class<?>> acceptClasses) {
        return Strings.isNullOrEmpty(yamlContent) ? new LinkedHashMap<>() : (Map) new Yaml(new ClassFilterConstructor(acceptClasses)).load(yamlContent);
    }
    
    /**
     * Unmarshal properties YAML.
     *
     * @param yamlContent YAML content
     * @param acceptClasses accept classes
     * @return properties from YAML
     */
    public static Properties unmarshalProperties(final String yamlContent, final Collection<Class<?>> acceptClasses) {
        return Strings.isNullOrEmpty(yamlContent) ? new Properties() : new Yaml(new ClassFilterConstructor(acceptClasses)).loadAs(yamlContent, Properties.class);
    }
    
    /**
     * Marshal YAML.
     *
     * @param value object to be marshaled
     * @return YAML content
     */
    public static String marshal(final Object value) {
        return new Yaml(new ShardingSphereYamlRepresenter()).dumpAsMap(value);
    }
}
