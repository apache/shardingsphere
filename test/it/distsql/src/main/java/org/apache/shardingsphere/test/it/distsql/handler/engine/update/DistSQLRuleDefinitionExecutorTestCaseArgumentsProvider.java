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

package org.apache.shardingsphere.test.it.distsql.handler.engine.update;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.core.featured.DistSQLParserEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.jaxb.DistSQLRuleDefinitionExecutorTestCase;
import org.apache.shardingsphere.test.it.distsql.handler.engine.update.jaxb.DistSQLRuleDefinitionExecutorTestCases;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DistSQL rule definition executor test case arguments provider.
 */
public final class DistSQLRuleDefinitionExecutorTestCaseArgumentsProvider implements ArgumentsProvider {
    
    @SneakyThrows(JAXBException.class)
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
        DistSQLRuleDefinitionExecutorSettings settings = extensionContext.getRequiredTestClass().getAnnotation(DistSQLRuleDefinitionExecutorSettings.class);
        Preconditions.checkNotNull(settings, "Annotation DistSQLRuleDefinitionExecutorSettings is required.");
        DistSQLRuleDefinitionExecutorTestCases testCases = (DistSQLRuleDefinitionExecutorTestCases) JAXBContext.newInstance(DistSQLRuleDefinitionExecutorTestCases.class)
                .createUnmarshaller().unmarshal(Thread.currentThread().getContextClassLoader().getResource(settings.value()));
        return testCases.getTestCases().stream().map(each -> Arguments.arguments(each.getDistSQL(), getCurrentRuleConfiguration(each),
                getDistSQLStatement(each), getExpectedRuleConfiguration(each), getExpectedException(each))).collect(Collectors.toList()).stream();
    }
    
    private DistSQLStatement getDistSQLStatement(final DistSQLRuleDefinitionExecutorTestCase testCase) {
        return (DistSQLStatement) new DistSQLParserEngine().parse(testCase.getDistSQL());
    }
    
    private GlobalRuleConfiguration getCurrentRuleConfiguration(final DistSQLRuleDefinitionExecutorTestCase testCase) {
        RuleConfiguration result = swapRuleConfiguration(testCase.getCurrentRuleConfigurationYAMLFile());
        Preconditions.checkState(result instanceof GlobalRuleConfiguration, "Current rule configuration is not a global rule configuration.");
        return (GlobalRuleConfiguration) result;
    }
    
    private RuleConfiguration getExpectedRuleConfiguration(final DistSQLRuleDefinitionExecutorTestCase testCase) {
        if (Strings.isNullOrEmpty(testCase.getExpectedRuleConfigurationYAMLFile())) {
            return null;
        }
        return swapRuleConfiguration(testCase.getExpectedRuleConfigurationYAMLFile());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Class<? extends Exception> getExpectedException(final DistSQLRuleDefinitionExecutorTestCase testCase) {
        if (Strings.isNullOrEmpty(testCase.getExpectedException())) {
            return null;
        }
        Class<?> result = Class.forName(testCase.getExpectedException());
        Preconditions.checkState(Exception.class.isAssignableFrom(result), "Expected exception must be an exception type.");
        return (Class<? extends Exception>) result;
    }
    
    @SneakyThrows(IOException.class)
    private RuleConfiguration swapRuleConfiguration(final String yamlFile) {
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(getYAMLFile(yamlFile), YamlRootConfiguration.class);
        Preconditions.checkState(!yamlRootConfig.getRules().isEmpty(), "Rule configuration YAML file `%s` is empty.", yamlFile);
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfiguration(yamlRootConfig.getRules().iterator().next());
    }
    
    private File getYAMLFile(final String yamlFile) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(yamlFile);
        assertNotNull(url);
        return new File(url.getFile());
    }
}
