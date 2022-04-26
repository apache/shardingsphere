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

package org.apache.shardingsphere.infra.metadata.ddlgenerator.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FreemarkerManager {
    
    private static final FreemarkerManager INSTANCE = new FreemarkerManager();
    
    private final Configuration templateConfig = createTemplateConfiguration();
    
    public static FreemarkerManager getInstance() {
        return INSTANCE;
    }
    
    @SneakyThrows
    private Configuration createTemplateConfiguration() {
        Configuration result = new Configuration(Configuration.VERSION_2_3_31);
        result.setDirectoryForTemplateLoading(new File(Objects.requireNonNull(FreemarkerManager.class.getClassLoader().getResource("template")).getFile()));
        result.setDefaultEncoding("UTF-8");
        return result;
    }
    
    @SneakyThrows
    public static String getSqlFromTemplate(final Map<String, Object> data, final String path) {
        Template template = FreemarkerManager.getInstance().templateConfig.getTemplate(path);
        try (StringWriter result = new StringWriter()) {
            template.process(data, result);
            return result.toString();
        }
    }
}
