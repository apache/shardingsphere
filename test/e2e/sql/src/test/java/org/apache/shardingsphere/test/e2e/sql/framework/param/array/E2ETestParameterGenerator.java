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

package org.apache.shardingsphere.test.e2e.sql.framework.param.array;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.ral.RALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.sql.cases.SQLE2ETestCasesXMLLoader;
import org.apache.shardingsphere.test.e2e.sql.cases.casse.SQLE2ETestCaseContext;
import org.apache.shardingsphere.test.e2e.sql.cases.casse.assertion.SQLE2ETestCaseAssertion;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.CaseTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.E2ETestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLExecuteType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * E2E test parameter generator.
 */
@RequiredArgsConstructor
public final class E2ETestParameterGenerator {
    
    private static final SQLE2ETestCasesXMLLoader TEST_CASES_LOADER = SQLE2ETestCasesXMLLoader.getInstance();
    
    private final Collection<String> envAdapters;
    
    private final Collection<String> envScenarios;
    
    private final Mode mode;
    
    private final Collection<DatabaseType> envDatabaseTypes;
    
    private final boolean smoke;
    
    /**
     * Get assertion test parameter.
     *
     * @param sqlCommandType SQL command type
     * @return assertion test parameter
     */
    public Collection<AssertionTestParameter> getAssertionTestParameter(final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (SQLE2ETestCaseContext each : TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            if (smoke) {
                result.addAll(getAssertionTestParameterFilterBySmoke(each, sqlCommandType));
            } else {
                result.addAll(getAssertionTestParameter(each, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionTestParameter> getAssertionTestParameter(final SQLE2ETestCaseContext testCaseContext, final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (DatabaseType each : getDatabaseTypes(testCaseContext.getTestCase().getDbTypes())) {
            if (envDatabaseTypes.contains(each)) {
                result.addAll(getAssertionTestParameter(testCaseContext, each, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionTestParameter> getAssertionTestParameter(final SQLE2ETestCaseContext testCaseContext,
                                                                         final DatabaseType databaseType, final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (SQLExecuteType each : SQLExecuteType.values()) {
            if (!sqlCommandType.isLiteralOnly() || SQLExecuteType.LITERAL == each) {
                result.addAll(getAssertionTestParameter(testCaseContext, databaseType, each, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionTestParameter> getAssertionTestParameter(final SQLE2ETestCaseContext testCaseContext,
                                                                         final DatabaseType databaseType, final SQLExecuteType sqlExecuteType, final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (SQLE2ETestCaseAssertion each : testCaseContext.getTestCase().getAssertions()) {
            result.addAll(getAssertionTestParameter(testCaseContext, databaseType, sqlExecuteType, each, sqlCommandType));
        }
        return result;
    }
    
    private Collection<AssertionTestParameter> getAssertionTestParameter(final SQLE2ETestCaseContext testCaseContext,
                                                                         final DatabaseType databaseType, final SQLExecuteType sqlExecuteType,
                                                                         final SQLE2ETestCaseAssertion assertion, final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (String each : getEnvAdapters(testCaseContext.getTestCase().getAdapters())) {
            if (sqlCommandType.getRunningAdaptors().contains(each) && envAdapters.contains(each)) {
                result.addAll(getAssertionTestParameter(testCaseContext, assertion, each, databaseType, sqlExecuteType, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<AssertionTestParameter> getAssertionTestParameter(final SQLE2ETestCaseContext testCaseContext, final SQLE2ETestCaseAssertion assertion,
                                                                         final String adapter, final DatabaseType databaseType,
                                                                         final SQLExecuteType sqlExecuteType, final SQLCommandType sqlCommandType) {
        Collection<String> scenarios = null == testCaseContext.getTestCase().getScenarioTypes() ? Collections.emptyList() : Arrays.asList(testCaseContext.getTestCase().getScenarioTypes().split(","));
        return envScenarios.stream().filter(each -> filterScenarios(each, scenarios, sqlCommandType.getSqlStatementClass()))
                .map(each -> new AssertionTestParameter(testCaseContext, assertion, adapter, each, mode, databaseType, sqlExecuteType, sqlCommandType)).collect(Collectors.toList());
    }
    
    private Collection<AssertionTestParameter> getAssertionTestParameterFilterBySmoke(final SQLE2ETestCaseContext testCaseContext, final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        if (testCaseContext.getTestCase().isSmoke()) {
            result.addAll(getAssertionTestParameter(testCaseContext, sqlCommandType));
        }
        return result;
    }
    
    private Collection<String> getEnvAdapters(final String envAdapters) {
        return Strings.isNullOrEmpty(envAdapters) ? this.envAdapters : Splitter.on(',').trimResults().splitToList(envAdapters);
    }
    
    private boolean filterScenarios(final String scenario, final Collection<String> scenarios, final Class<? extends SQLStatement> sqlStatementClass) {
        if (sqlStatementClass == RALStatement.class) {
            return "empty_rules".equals(scenario);
        }
        if (sqlStatementClass == RDLStatement.class || "distsql_rdl".equals(scenario)) {
            return sqlStatementClass == RDLStatement.class && "distsql_rdl".equals(scenario);
        }
        if ("empty_rules".equals(scenario)) {
            return false;
        }
        return scenarios.isEmpty() || scenarios.contains(scenario);
    }
    
    /**
     * Get case test parameter.
     *
     * @param sqlCommandType SQL command type
     * @return case test parameter
     */
    public Collection<E2ETestParameter> getCaseTestParameter(final SQLCommandType sqlCommandType) {
        Collection<E2ETestParameter> result = new LinkedList<>();
        for (SQLE2ETestCaseContext each : TEST_CASES_LOADER.getTestCaseContexts(sqlCommandType)) {
            result.addAll(getCaseTestParameter(each, sqlCommandType));
        }
        return result;
    }
    
    private Collection<E2ETestParameter> getCaseTestParameter(final SQLE2ETestCaseContext testCaseContext, final SQLCommandType sqlCommandType) {
        Collection<E2ETestParameter> result = new LinkedList<>();
        for (DatabaseType each : getDatabaseTypes(testCaseContext.getTestCase().getDbTypes())) {
            if (envDatabaseTypes.contains(each)) {
                result.addAll(getCaseTestParameter(testCaseContext, each, sqlCommandType));
            }
        }
        return result;
    }
    
    private Collection<E2ETestParameter> getCaseTestParameter(final SQLE2ETestCaseContext testCaseContext, final DatabaseType databaseType, final SQLCommandType sqlCommandType) {
        Collection<E2ETestParameter> result = new LinkedList<>();
        if (testCaseContext.getTestCase().isSkipBatch()) {
            return Collections.emptyList();
        }
        for (String each : envAdapters) {
            result.addAll(getCaseTestParameter(testCaseContext, each, databaseType, sqlCommandType));
        }
        return result;
    }
    
    private Collection<E2ETestParameter> getCaseTestParameter(final SQLE2ETestCaseContext testCaseContext, final String adapter,
                                                              final DatabaseType databaseType, final SQLCommandType sqlCommandType) {
        if (null != testCaseContext.getTestCase().getAdapters() && !testCaseContext.getTestCase().getAdapters().contains(adapter)) {
            return Collections.emptyList();
        }
        Collection<String> scenarios = null == testCaseContext.getTestCase().getScenarioTypes() ? Collections.emptyList() : Arrays.asList(testCaseContext.getTestCase().getScenarioTypes().split(","));
        return envScenarios.stream().filter(each -> scenarios.isEmpty() || scenarios.contains(each))
                .map(each -> new CaseTestParameter(testCaseContext, adapter, each, mode, databaseType, sqlCommandType)).collect(Collectors.toList());
    }
    
    private Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        String candidates = Strings.isNullOrEmpty(databaseTypes) ? "MySQL,Oracle,SQLServer,PostgreSQL,openGauss,Hive" : databaseTypes;
        return Splitter.on(',').trimResults().splitToList(candidates).stream().map(each -> TypedSPILoader.getService(DatabaseType.class, each)).collect(Collectors.toList());
    }
}
