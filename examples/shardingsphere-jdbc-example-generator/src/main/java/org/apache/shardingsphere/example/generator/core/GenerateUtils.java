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
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Freemarker generate utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenerateUtils {
    
    /**
     * Generate directories.
     * 
     * @param templateConfig template configuration
     * @param dataModel data model
     * @param paths paths
     * @param outputPath output path
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generateDirs(final Configuration templateConfig, final Map<String, String> dataModel, final Collection<String> paths, final String outputPath) throws IOException, TemplateException {
        if (null == paths || paths.isEmpty()) {
            new File(generatePath(templateConfig, dataModel, outputPath)).mkdirs();
            return;
        }
        for (String each : paths) {
            new File(generatePath(templateConfig, dataModel, outputPath + "/" + each)).mkdirs();
        }
    }
    
    /**
     * Generate file.
     *
     * @param templateConfig template configuration
     * @param rootDir generator root directory
     * @param dataModel data model
     * @param path paths
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    public static void generateFile(final Configuration templateConfig, final String rootDir, final Map<String, String> dataModel, final Map<String, String> templateMap, final String path) throws IOException, TemplateException {
        String outputPath = generatePath(templateConfig, dataModel, path);
        for (Entry<String, String> entry : templateMap.entrySet()) {
            processFile(templateConfig, dataModel, rootDir + "/" + entry.getKey(), outputPath + "/" + entry.getValue());
        }
    }
    
    /**
     * Generate path.
     *
     * @param templateConfig template configuration
     * @param model model
     * @param relativePath relative path
     * @return Path generated from the model
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    public static String generatePath(final Configuration templateConfig, final Object model, final String relativePath) throws IOException, TemplateException {
        try (StringWriter result = new StringWriter()) {
            new Template("path", relativePath, templateConfig).process(model, result);
            return result.toString();
        }
    }
    
    /**
     * Generate file.
     *
     * @param templateConfig template configuration
     * @param model data model
     * @param templateFile template file
     * @param outputFile output file
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    public static void processFile(final Configuration templateConfig, final Object model, final String templateFile, final String outputFile) throws IOException, TemplateException {
        try (Writer writer = new FileWriter(outputFile)) {
            templateConfig.getTemplate(templateFile).process(model, writer);
        }
    }
    
    /**
     * Combination generator.
     * 
     * @param combinations combinations
     * @return All combination
     */
    public static Collection<String> generateCombination(List<String> combinations) {
        int len = combinations.size();
        Collection<String> result = new HashSet<>();
        for (int i = 0, size = 1 << len; i < size; i++) {
            StringBuilder eachCombBuilder = new StringBuilder();
            for (int j = 0; j < len; j++) {
                if (0 != ((1 << j) & i)) {
                    eachCombBuilder.append(combinations.get(j)).append(",");
                }
            }
            if (0 != eachCombBuilder.length()) {
                eachCombBuilder.deleteCharAt(eachCombBuilder.lastIndexOf(","));
                result.add(eachCombBuilder.toString());
            }
        }
        return result;
    }
}
