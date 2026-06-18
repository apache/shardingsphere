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

package org.apache.shardingsphere.test.it.distsql.handler.engine.query;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.core.featured.DistSQLParserEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.jaxb.DistSQLRuleQueryExecutorTestCase;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.jaxb.DistSQLRuleQueryExecutorTestCases;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DistSQL rule query executor test case arguments provider.
 */
public final class DistSQLRuleQueryExecutorTestCaseArgumentsProvider implements ArgumentsProvider {
    
    @SneakyThrows(JAXBException.class)
    @Override
    public Stream<Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
        DistSQLRuleQueryExecutorSettings settings = context.getRequiredTestClass().getAnnotation(DistSQLRuleQueryExecutorSettings.class);
        Preconditions.checkNotNull(settings, "Annotation DistSQLRuleQueryExecutorSettings is required.");
        DistSQLRuleQueryExecutorTestCases testCases = (DistSQLRuleQueryExecutorTestCases) JAXBContext.newInstance(DistSQLRuleQueryExecutorTestCases.class)
                .createUnmarshaller().unmarshal(Thread.currentThread().getContextClassLoader().getResource(settings.value()));
        return testCases.getTestCases().stream().map(each -> Arguments.arguments(
                each.getDistSQL(), getDistSQLStatement(each), getCurrentRuleConfiguration(each), getExpectedQueryResultRows(each))).collect(Collectors.toList()).stream();
    }
    
    private DistSQLStatement getDistSQLStatement(final DistSQLRuleQueryExecutorTestCase testCase) {
        return (DistSQLStatement) new DistSQLParserEngine().parse(testCase.getDistSQL());
    }
    
    @SneakyThrows(IOException.class)
    private RuleConfiguration getCurrentRuleConfiguration(final DistSQLRuleQueryExecutorTestCase testCase) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(testCase.getCurrentRuleConfigurationYAMLFile());
        assertNotNull(url);
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfiguration(yamlRootConfig.getRules().iterator().next());
    }
    
    private Collection<LocalDataQueryResultRow> getExpectedQueryResultRows(final DistSQLRuleQueryExecutorTestCase testCase) {
        return testCase.getExpectedQueryResultRows().stream().map(each -> new LocalDataQueryResultRow(Splitter.on('|').trimResults().splitToStream(each).toArray())).collect(Collectors.toList());
    }
}
