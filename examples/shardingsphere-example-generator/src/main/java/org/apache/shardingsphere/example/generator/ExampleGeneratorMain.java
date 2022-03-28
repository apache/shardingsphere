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

package org.apache.shardingsphere.example.generator;

import freemarker.template.TemplateException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.generator.core.ExampleGeneratorFactory;

import java.io.IOException;

/**
 * Example generator entrance.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ExampleGeneratorMain {
    
    /**
     * Main entrance.
     * 
     * @param args args
     * @throws IOException IO exception
     * @throws TemplateException template exception
     */
    public static void main(final String[] args) throws IOException, TemplateException {
        new ExampleGeneratorFactory().generate();
        printMessages();
    }
    
    private static void printMessages() {
        log.info("Example codes are generated successful!");
        log.info("Please find them in folder `target/generated-sources/shardingsphere-${product}-sample`.");
    }
}
