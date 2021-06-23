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

package org.apache.shardingsphere.test.integration.engine.it;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.watcher.ITWatcher;
import org.junit.Rule;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
public abstract class SingleITCase extends BaseITCase {
    
    @Rule
    public ITWatcher watcher = new ITWatcher();
    
    private final SQLExecuteType sqlExecuteType;
    
    private final IntegrationTestCaseAssertion assertion;
    
    private final DataSet dataSet;
    
    public SingleITCase(final AssertionParameterizedArray parameter) {
        super(parameter);
        this.sqlExecuteType = parameter.getSqlExecuteType();
        this.assertion = parameter.getAssertion();
        this.dataSet = null == assertion ? null : DataSetLoader.load(parameter.getTestCaseContext().getParentPath(), getScenario(), getDatabaseType(), assertion.getExpectedDataFile());
    }
    
    protected final String getSQL() throws ParseException {
        return sqlExecuteType == SQLExecuteType.Literal ? getLiteralSQL(getIntegrationTestCase().getSql()) : getIntegrationTestCase().getSql();
    }
    
    protected final String getLiteralSQL(final String sql) throws ParseException {
        List<Object> parameters = null == assertion ? Collections.emptyList() : assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList());
        return parameters.isEmpty() ? sql : String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%").replace("%%", "%").replace("'%'", "'%%'");
    }
}
