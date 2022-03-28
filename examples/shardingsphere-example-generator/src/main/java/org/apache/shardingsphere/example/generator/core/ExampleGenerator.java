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

package org.apache.shardingsphere.example.generator.core;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * Example generator.
 */
public interface ExampleGenerator {
    
    String OUTPUT_PATH = "./examples/shardingsphere-example-generator/target/generated-sources/shardingsphere-${product}-sample/${feature?replace(',', '-')}--${framework}--${mode}--${transaction}/";
    
    String RESOURCES_PATH = "src/main/resources";
    
    /**
     * Generate file.
     * 
     * @param templateConfig template configuration
     * @param dataModel data model
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    void generate(Configuration templateConfig, Map<String, String> dataModel) throws IOException, TemplateException;
    
    /**
     * Get generator type.
     *
     * @return generator type
     */
    String getType();
}
