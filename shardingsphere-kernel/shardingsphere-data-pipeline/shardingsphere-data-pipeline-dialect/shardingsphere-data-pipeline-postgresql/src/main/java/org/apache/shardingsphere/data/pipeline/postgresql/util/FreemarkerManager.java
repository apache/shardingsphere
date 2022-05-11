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

package org.apache.shardingsphere.data.pipeline.postgresql.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Freemarker manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FreemarkerManager {
    
    private static final FreemarkerManager INSTANCE = new FreemarkerManager();
    
    private static final Map<Integer, String> PG_VERSION_PATHS = new LinkedHashMap<>();
    
    static {
        PG_VERSION_PATHS.put(120000, "12_plus");
        PG_VERSION_PATHS.put(110000, "11_plus");
        PG_VERSION_PATHS.put(100000, "10_plus");
        PG_VERSION_PATHS.put(90600, "9.6_plus");
        PG_VERSION_PATHS.put(90500, "9.5_plus");
        PG_VERSION_PATHS.put(90400, "9.4_plus");
        PG_VERSION_PATHS.put(90300, "9.3_plus");
        PG_VERSION_PATHS.put(90200, "9.2_plus");
        PG_VERSION_PATHS.put(90100, "9.1_plus");
        PG_VERSION_PATHS.put(90000, "9.0_plus");
        PG_VERSION_PATHS.put(0, "default");
    }
    
    private final Configuration templateConfig = createTemplateConfiguration();
    
    /**
     * Get freemarker manager instance.
     * 
     * @return freemarker manager instance
     */
    public static FreemarkerManager getInstance() {
        return INSTANCE;
    }
    
    @SneakyThrows
    private Configuration createTemplateConfiguration() {
        Configuration result = new Configuration(Configuration.VERSION_2_3_31);
        result.setClassForTemplateLoading(getClass(), "/template");
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    /**
     * Get sql by postgres version.
     * 
     * @param data data
     * @param pathFormat path format
     * @param majorVersion major version
     * @param minorVersion minor version
     * @return sql
     */
    @SneakyThrows
    public static String getSqlByPgVersion(final Map<String, Object> data, final String pathFormat, final int majorVersion, final int minorVersion) {
        int version = majorVersion * 10000 + minorVersion;
        try (StringWriter result = new StringWriter()) {
            findTemplate(pathFormat, version).orElseThrow(() -> new ShardingSphereException("Failed to get template, path:%s, version:%s", pathFormat, version)).process(data, result);
            return result.toString();
        }
    }
    
    private static Optional<Template> findTemplate(final String pathFormat, final int version) throws IOException {
        for (Entry<Integer, String> entry : PG_VERSION_PATHS.entrySet()) {
            if (entry.getKey() > version) {
                continue;
            }
            try {
                Template template = FreemarkerManager.getInstance().templateConfig.getTemplate(String.format(pathFormat, entry.getValue()));
                return Optional.of(template);
            } catch (TemplateNotFoundException ignored) {
            }
        }
        return Optional.empty();
    }
}
