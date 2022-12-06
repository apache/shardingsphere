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
import org.apache.shardingsphere.test.e2e.framework.watcher.ITWatcher;
import org.junit.Rule;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
public abstract class SingleITCase extends BaseITCase {
    
    @Rule
    @Getter(AccessLevel.NONE)
    public ITWatcher watcher = new ITWatcher();
    
    private final SQLExecuteType sqlExecuteType;
    
    private final IntegrationTestCaseAssertion assertion;
    
    private final DataSet dataSet;
    
    public SingleITCase(final AssertionTestParameter testParameter) {
        super(testParameter);
        sqlExecuteType = testParameter.getSqlExecuteType();
        assertion = testParameter.getAssertion();
        dataSet = null == assertion || null == assertion.getExpectedDataFile()
                ? null
                : DataSetLoader.load(testParameter.getTestCaseContext().getParentPath(), getScenario(), getDatabaseType(), getMode(), assertion.getExpectedDataFile());
    }
    
    protected final String getSQL() throws ParseException {
        return sqlExecuteType == SQLExecuteType.Literal ? getLiteralSQL(getItCase().getSql()) : getItCase().getSql();
    }
    
    private String getLiteralSQL(final String sql) throws ParseException {
        List<Object> params = null == assertion ? Collections.emptyList() : assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList());
        return params.isEmpty() ? sql : String.format(sql.replace("%", "$").replace("?", "%s"), params.toArray()).replace("$", "%").replace("%%", "%").replace("'%'", "'%%'");
    }
}
