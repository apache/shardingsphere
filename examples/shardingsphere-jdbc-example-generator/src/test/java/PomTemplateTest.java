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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/**
 * Test case for pom template generation.
 */
public final class PomTemplateTest {
    
    private Configuration templateConfig;
    
    @BeforeEach
    public void setUp() {
        templateConfig = new Configuration(Configuration.VERSION_2_3_31);
        templateConfig.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "/template");
    }
    
    @Test
    public void assertStandaloneJdbcRepository() throws IOException, TemplateException {
        Map<String, Object> dataModel = new HashMap<>(6, 1F);
        dataModel.put("mode", "standalone");
        dataModel.put("repository", "JDBC");
        dataModel.put("feature", "sharding");
        dataModel.put("framework", "spring-boot-starter-jdbc");
        dataModel.put("transaction", "local");
        dataModel.put("shardingsphereVersion", "5.5.3-SNAPSHOT");
        Template template = templateConfig.getTemplate("pom.ftl");
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        assertThat(writer.toString(), containsString("shardingsphere-standalone-mode-repository-jdbc"));
    }
    
    @Test
    public void assertNoStandaloneJdbcRepository() throws IOException, TemplateException {
        Map<String, Object> dataModel = new HashMap<>(6, 1F);
        dataModel.put("mode", "standalone");
        dataModel.put("repository", "File");
        dataModel.put("feature", "sharding");
        dataModel.put("framework", "spring-boot-starter-jdbc");
        dataModel.put("transaction", "local");
        dataModel.put("shardingsphereVersion", "5.5.3-SNAPSHOT");
        Template template = templateConfig.getTemplate("pom.ftl");
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        assertThat(writer.toString(), not(containsString("shardingsphere-standalone-mode-repository-jdbc")));
    }
}
