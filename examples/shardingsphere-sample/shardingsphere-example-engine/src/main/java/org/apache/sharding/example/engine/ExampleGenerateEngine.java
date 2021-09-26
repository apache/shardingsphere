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

package org.apache.sharding.example.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Example generate engine.
 */
public final class ExampleGenerateEngine {
    
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_31);
    
    public ExampleGenerateEngine(final String path) throws IOException {
        CONFIGURATION.setDirectoryForTemplateLoading(new File(path));
        CONFIGURATION.setDefaultEncoding("UTF-8");
    }
    
    /**
     * Generate files based on data model.
     * @param obj data model
     * @param templateFile Equivalent to the template name of the template base directory.
     * @param outputFile Output directory and file name
     */
    public void process(final Object obj, final String templateFile, final String outputFile) {
        try {
            Template template = CONFIGURATION.getTemplate(templateFile);
            template.process(obj, new FileWriter(outputFile));
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
}

