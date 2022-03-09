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

package org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.rewrite.parameterized.entity.RewriteAssertionEntity;
import org.apache.shardingsphere.sharding.rewrite.parameterized.entity.RewriteAssertionsRootEntity;
import org.apache.shardingsphere.sharding.rewrite.parameterized.entity.RewriteOutputEntity;
import org.apache.shardingsphere.sharding.rewrite.parameterized.loader.RewriteAssertionsRootEntityLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Test parameters for SQL rewrite engine builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLRewriteEngineTestParametersBuilder {
    
    /**
     * Load test parameters.
     * 
     * @param type type
     * @param path path
     * @param targetClass target class
     * @return Test parameters list for SQL rewrite engine
     */
    public static Collection<Object[]> loadTestParameters(final String type, final String path, final Class<?> targetClass) {
        Collection<Object[]> result = new LinkedList<>();
        for (Entry<String, RewriteAssertionsRootEntity> entry : loadAllRewriteAssertionsRootEntities(type, path, targetClass).entrySet()) {
            result.addAll(createTestParameters(type, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private static Map<String, RewriteAssertionsRootEntity> loadAllRewriteAssertionsRootEntities(final String type, final String path, final Class<?> targetClass) {
        Map<String, RewriteAssertionsRootEntity> result = new LinkedHashMap<>();
        File file = new File(targetClass.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + path);
        for (File each : Objects.requireNonNull(file.listFiles())) {
            if (each.isFile()) {
                appendFromFile(type, each, path, result);
            } else {
                appendFromDirectory(type, each, path + "/" + each.getName(), result);
            }
        }
        return result;
    }
    
    private static void appendFromDirectory(final String type, final File directory, final String path, final Map<String, RewriteAssertionsRootEntity> result) {
        for (File each : Objects.requireNonNull(directory.listFiles())) {
            if (each.isFile()) {
                appendFromFile(type, each, path, result);
            } else {
                appendFromDirectory(type, each, path + "/" + each.getName(), result);
            }
        }
    }
    
    private static void appendFromFile(final String type, final File file, final String path, final Map<String, RewriteAssertionsRootEntity> result) {
        if (file.getName().endsWith(".xml")) {
            String key = path.toLowerCase().replace(type.toLowerCase() + "/", "") + "/" + file.getName();
            result.put(key, new RewriteAssertionsRootEntityLoader().load(path + "/" + file.getName()));
        }
    }
    
    private static Collection<Object[]> createTestParameters(final String type, final String fileName, final RewriteAssertionsRootEntity rootAssertions) {
        Collection<Object[]> result = new LinkedList<>();
        for (RewriteAssertionEntity each : rootAssertions.getAssertions()) {
            for (String databaseType : getDatabaseTypes(each.getDatabaseTypes())) {
                result.add(new SQLRewriteEngineTestParameters(type, each.getId(), fileName, rootAssertions.getYamlRule(), each.getInput().getSql(),
                        createInputParameters(each.getInput().getParameters()), createOutputSQLs(each.getOutputs()), createOutputGroupedParameters(each.getOutputs()), databaseType).toArray());   
            }
        }
        return result;
    }
    
    private static Collection<String> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? getAllDatabaseTypes() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private static Collection<String> getAllDatabaseTypes() {
        return Arrays.asList("MySQL", "PostgreSQL", "Oracle", "SQLServer", "SQL92", "openGauss");
    }
    
    private static List<Object> createInputParameters(final String inputParameters) {
        if (null == inputParameters) {
            return Collections.emptyList();
        } else {
            return Splitter.on(",").trimResults().splitToList(inputParameters).stream().map(input -> {
                Object result = Ints.tryParse(input);
                return result == null ? input : result;
            }).collect(Collectors.toList());
        }
    }
    
    private static List<String> createOutputSQLs(final List<RewriteOutputEntity> outputs) {
        List<String> result = new ArrayList<>(outputs.size());
        for (RewriteOutputEntity each : outputs) {
            result.add(each.getSql());
        }
        return result;
    }
    
    private static List<List<String>> createOutputGroupedParameters(final List<RewriteOutputEntity> outputs) {
        List<List<String>> result = new ArrayList<>(outputs.size());
        for (RewriteOutputEntity each : outputs) {
            result.add(null == each.getParameters() ? Collections.emptyList() : Splitter.on(",").trimResults().splitToList(each.getParameters()));
        }
        return result;
    }
}
