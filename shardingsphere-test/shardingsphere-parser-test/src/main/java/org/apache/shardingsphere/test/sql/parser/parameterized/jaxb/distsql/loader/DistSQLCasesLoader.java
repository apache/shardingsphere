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

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.distsql.loader;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.Case;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.distsql.DistSQLCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.distsql.DistSQLCases;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.parser.parameterized.loader.CasesLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * DistSQL test cases loader.
 */
public final class DistSQLCasesLoader extends CasesLoader {
    
    public DistSQLCasesLoader(final String rootDirection) {
        super(rootDirection);
    }
    
    @Override
    public void buildCaseMap(final Map<String, Case> sqlCaseMap, final InputStream inputStream) throws JAXBException {
        DistSQLCases sqlCases = (DistSQLCases) JAXBContext.newInstance(DistSQLCases.class).createUnmarshaller().unmarshal(inputStream);
        for (DistSQLCase each : sqlCases.getDistSQLCases()) {
            Preconditions.checkState(!sqlCaseMap.containsKey(each.getId()), "Find duplicated Case ID: %s", each.getId());
            sqlCaseMap.put(each.getId(), each);
        }
    }
    
    @Override
    public Collection<Object[]> getTestParameters(final Collection<String> databaseTypes) {
        Collection<Object[]> result = new LinkedList<>();
        for (Case each : super.getCases().values()) {
            Object[] parameters = new Object[1];
            parameters[0] = each.getId();
            result.add(parameters);
        }
        return result;
    }
    
    @Override
    public String getCaseValue(final String sqlCaseId, final SQLCaseType sqlCaseType, final List<?> parameters, final String databaseType) {
        Map<String, Case> sqlCaseMap = super.getCases();
        Preconditions.checkState(sqlCaseMap.containsKey(sqlCaseId), "Can not find case of ID: %s", sqlCaseId);
        DistSQLCase statement = (DistSQLCase) sqlCaseMap.get(sqlCaseId);
        return statement.getValue();
    }
}
