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

package org.apache.shardingsphere.test.integration.framework.param.array;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.SQLExecuteType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.param.model.CaseParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Parameterized array generator.
 */
@RequiredArgsConstructor
public final class ParameterizedArrayGenerator {
    
    private static final IntegrationTestCasesLoader TEST_CASES_LOADER = IntegrationTestCasesLoader.getInstance();
    
    private final Collection<String> envAdapters;
    
    private final Collection<String> envScenarios;
    
    private final String envMode;
    
    private final Collection<DatabaseType> envDatabaseTypes;
    
    /**
     * Get assertion parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return assertion parameterized array
     */
    public Collection<AssertionParameterizedArray> getAssertionParameterized(final SQLCommandType sqlCommandType) {
        Collection<AssertionParameterizedArray> result = new LinkedList<>();
        for (IntegrationTestCaseContext each : TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            result.addAll(getAssertionParameterizedArray(each, sqlCommandType));
        }
        return result;
    }
    
    private Collection<AssertionParameterizedArray> getAssertionParameterizedArray(final IntegrationTestCaseContext testCaseContext, final SQLCommandType sqlCommandType) {
        Collection<AssertionParameterizedArray> result = new LinkedList<>();
        for (DatabaseType each : getDatabaseTypes(testCaseContext.getTestCase().getDbTypes())) {
            if (envDatabaseTypes.contains(each)) {
                result.addAll(getAssertionParameterizedArray(testCaseContext, each, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionParameterizedArray> getAssertionParameterizedArray(final IntegrationTestCaseContext testCaseContext,
                                                                                   final DatabaseType databaseType, final SQLCommandType sqlCommandType) {
        Collection<AssertionParameterizedArray> result = new LinkedList<>();
        for (SQLExecuteType each : SQLExecuteType.values()) {
            if (!sqlCommandType.isLiteralOnly() || SQLExecuteType.Literal == each) {
                result.addAll(getAssertionParameterizedArray(testCaseContext, databaseType, each, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionParameterizedArray> getAssertionParameterizedArray(final IntegrationTestCaseContext testCaseContext,
                                                                                   final DatabaseType databaseType, final SQLExecuteType sqlExecuteType, final SQLCommandType sqlCommandType) {
        Collection<AssertionParameterizedArray> result = new LinkedList<>();
        for (IntegrationTestCaseAssertion each : testCaseContext.getTestCase().getAssertions()) {
            result.addAll(getAssertionParameterizedArray(testCaseContext, databaseType, sqlExecuteType, each, sqlCommandType));
        }
        return result;
    }
    
    private Collection<AssertionParameterizedArray> getAssertionParameterizedArray(final IntegrationTestCaseContext testCaseContext,
                                                                                   final DatabaseType databaseType, final SQLExecuteType sqlExecuteType,
                                                                                   final IntegrationTestCaseAssertion assertion, final SQLCommandType sqlCommandType) {
        Collection<AssertionParameterizedArray> result = new LinkedList<>();
        for (String each : envAdapters) {
            if (sqlCommandType.getRunningAdaptors().contains(each)) {
                result.addAll(getAssertionParameterizedArray(testCaseContext, assertion, each, databaseType, sqlExecuteType, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionParameterizedArray> getAssertionParameterizedArray(final IntegrationTestCaseContext testCaseContext, final IntegrationTestCaseAssertion assertion,
                                                                                   final String adapter, final DatabaseType databaseType,
                                                                                   final SQLExecuteType sqlExecuteType, final SQLCommandType sqlCommandType) {
        Collection<String> scenarios = null == testCaseContext.getTestCase().getScenarioTypes() ? Collections.emptyList() : Arrays.asList(testCaseContext.getTestCase().getScenarioTypes().split(","));
        return envScenarios.stream().filter(each -> filterScenarios(each, scenarios, sqlCommandType.getSqlStatementClass()))
                .map(each -> new AssertionParameterizedArray(testCaseContext, assertion, adapter, each, envMode, databaseType, sqlExecuteType)).collect(Collectors.toList());
    }
    
    private boolean filterScenarios(final String scenario, final Collection<String> scenarios, final Class<? extends SQLStatement> sqlStatementClass) {
        if (sqlStatementClass == RDLStatement.class || sqlStatementClass == RALStatement.class) {
            return "empty_rules".equals(scenario);
        }
        if ("empty_rules".equals(scenario)) {
            return false;
        }
        return scenarios.isEmpty() || scenarios.contains(scenario);
    }
    
    /**
     * Get case parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return case parameterized array
     */
    public Collection<ParameterizedArray> getCaseParameterized(final SQLCommandType sqlCommandType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (IntegrationTestCaseContext each : TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            result.addAll(getCaseParameterizedArray(each));
        }
        return result;
    }
    
    private Collection<ParameterizedArray> getCaseParameterizedArray(final IntegrationTestCaseContext testCaseContext) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (DatabaseType each : getDatabaseTypes(testCaseContext.getTestCase().getDbTypes())) {
            if (envDatabaseTypes.contains(each)) {
                result.addAll(getCaseParameterizedArray(testCaseContext, each));
            }
        }
        return result;
    }
    
    private Collection<ParameterizedArray> getCaseParameterizedArray(final IntegrationTestCaseContext testCaseContext, final DatabaseType databaseType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (String adapter : envAdapters) {
            result.addAll(getCaseParameterizedArray(testCaseContext, adapter, databaseType));
        }
        return result;
    }
    
    private Collection<ParameterizedArray> getCaseParameterizedArray(final IntegrationTestCaseContext testCaseContext, final String adapter, final DatabaseType databaseType) {
        Collection<String> scenarios = null == testCaseContext.getTestCase().getScenarioTypes() ? Collections.emptyList() : Arrays.asList(testCaseContext.getTestCase().getScenarioTypes().split(","));
        return envScenarios.stream().filter(each -> scenarios.isEmpty() || scenarios.contains(each))
                .map(each -> new CaseParameterizedArray(testCaseContext, adapter, each, envMode, databaseType)).collect(Collectors.toList());
    }
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        String candidates = Strings.isNullOrEmpty(databaseTypes) ? "H2,MySQL,Oracle,SQLServer,PostgreSQL" : databaseTypes;
        return Splitter.on(',').trimResults().splitToList(candidates).stream().map(DatabaseTypeFactory::getInstance).collect(Collectors.toList());
    }
}
