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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.integration.engine.watcher.ITWatcher;
import org.junit.Rule;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
public abstract class SingleIT extends BaseIT {
    
    @Rule
    public ITWatcher watcher = new ITWatcher();
    
    private final String caseIdentifier;
    
    private final IntegrationTestCaseAssertion assertion;
    
    private final SQLExecuteType sqlExecuteType;
    
    private final DataSet dataSet;
    
    private final String sql;
    
    protected SingleIT(final String parentPath, final IntegrationTestCaseAssertion assertion, final String adapter, final String scenario,
                       final DatabaseType databaseType, final SQLExecuteType sqlExecuteType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(adapter, scenario, databaseType);
        caseIdentifier = sql;
        this.assertion = assertion;
        this.sqlExecuteType = sqlExecuteType;
        this.sql = sqlExecuteType == SQLExecuteType.Literal ? getLiteralSQL(sql) : sql;
        dataSet = null == assertion ? null : DataSetLoader.load(parentPath, scenario, databaseType, assertion.getExpectedDataFile());
    }
    
    private String getLiteralSQL(final String sql) throws ParseException {
        List<Object> parameters = null == assertion ? Collections.emptyList() : assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList());
        return parameters.isEmpty() ? sql : String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%").replace("%%", "%").replace("'%'", "'%%'");
    }
}
