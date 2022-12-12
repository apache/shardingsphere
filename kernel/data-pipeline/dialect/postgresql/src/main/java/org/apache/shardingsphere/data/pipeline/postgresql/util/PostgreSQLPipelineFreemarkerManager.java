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
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * PostgreSQL pipeline freemarker manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPipelineFreemarkerManager {
    
    private static final Map<Integer, String> VERSION_PATHS = new LinkedHashMap<>();
    
    private static final Configuration TEMPLATE_CONFIG = createTemplateConfiguration();
    
    static {
        VERSION_PATHS.put(120000, "12_plus");
        VERSION_PATHS.put(110000, "11_plus");
        VERSION_PATHS.put(100000, "10_plus");
        VERSION_PATHS.put(90600, "9.6_plus");
        VERSION_PATHS.put(90500, "9.5_plus");
        VERSION_PATHS.put(90400, "9.4_plus");
        VERSION_PATHS.put(90300, "9.3_plus");
        VERSION_PATHS.put(90200, "9.2_plus");
        VERSION_PATHS.put(90100, "9.1_plus");
        VERSION_PATHS.put(90000, "9.0_plus");
    }
    
    private static Configuration createTemplateConfiguration() {
        Configuration result = new Configuration(Configuration.VERSION_2_3_31);
        result.setClassForTemplateLoading(PostgreSQLPipelineFreemarkerManager.class, "/template");
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    /**
     * Get SQL by PostgreSQL version.
     *
     * @param dataModel data model of template
     * @param pathFormat path format
     * @param majorVersion major version
     * @param minorVersion minor version
     * @return SQL
     */
    @SneakyThrows({IOException.class, TemplateException.class})
    public static String getSQLByVersion(final Map<String, Object> dataModel, final String pathFormat, final int majorVersion, final int minorVersion) {
        int version = majorVersion * 10000 + minorVersion;
        try (StringWriter result = new StringWriter()) {
            findTemplate(pathFormat, version).process(dataModel, result);
            return result.toString();
        }
    }
    
    private static Template findTemplate(final String pathFormat, final int version) throws IOException {
        for (Entry<Integer, String> entry : VERSION_PATHS.entrySet()) {
            if (entry.getKey() > version) {
                continue;
            }
            try {
                return TEMPLATE_CONFIG.getTemplate(String.format(pathFormat, entry.getValue()));
            } catch (TemplateNotFoundException ignored) {
            }
        }
        return TEMPLATE_CONFIG.getTemplate(String.format(pathFormat, "default"));
    }
}
