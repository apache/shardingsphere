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

package org.apache.shardingsphere.test.e2e.engine;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.test.e2e.cases.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSet;
import org.apache.shardingsphere.test.e2e.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.framework.runner.ParallelParameterized;
import org.apache.shardingsphere.test.e2e.framework.watcher.E2EWatcher;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(ParallelParameterized.class)
@Getter(AccessLevel.PROTECTED)
public abstract class SingleE2EIT {
    
    @Rule
    @Getter(AccessLevel.NONE)
    public E2EWatcher watcher = new E2EWatcher();
    
    private final AssertionTestParameter testParam;
            
    private final E2EContainerComposer containerComposer;
    
    private final SQLExecuteType sqlExecuteType;
    
    private final IntegrationTestCaseAssertion assertion;
    
    private final DataSet dataSet;
    
    private final DataSet generatedKeyDataSet;
    
    public SingleE2EIT(final AssertionTestParameter testParam) {
        this.testParam = testParam;
        containerComposer = new E2EContainerComposer(testParam);
        sqlExecuteType = testParam.getSqlExecuteType();
        assertion = testParam.getAssertion();
        dataSet = null == assertion || null == assertion.getExpectedDataFile()
                ? null
                : DataSetLoader.load(testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), assertion.getExpectedDataFile());
        generatedKeyDataSet = null == assertion || null == assertion.getExpectedGeneratedKeyDataFile()
                ? null
                : DataSetLoader.load(
                        testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), assertion.getExpectedGeneratedKeyDataFile());
    }
    
    protected final String getSQL() throws ParseException {
        return sqlExecuteType == SQLExecuteType.Literal ? getLiteralSQL(testParam.getTestCaseContext().getTestCase().getSql()) : testParam.getTestCaseContext().getTestCase().getSql();
    }
    
    private String getLiteralSQL(final String sql) throws ParseException {
        List<Object> params = null == assertion ? Collections.emptyList() : assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList());
        return params.isEmpty() ? sql : String.format(sql.replace("%", "ÿ").replace("?", "%s"), params.toArray()).replace("ÿ", "%").replace("%%", "%").replace("'%'", "'%%'");
    }
    
    @AfterClass
    public static void closeContainers() {
        E2EContainerComposer.closeContainers();
    }
}
