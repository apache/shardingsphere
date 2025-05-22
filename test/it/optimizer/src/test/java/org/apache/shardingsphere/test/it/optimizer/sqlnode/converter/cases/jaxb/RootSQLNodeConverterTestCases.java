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

package org.apache.shardingsphere.test.it.optimizer.sqlnode.converter.cases.jaxb;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL node converter test cases for XML root tag.
 */
@XmlRootElement(name = "sql-node-converter-test-cases")
@Getter
public final class RootSQLNodeConverterTestCases {
    
    @XmlElement(name = "test-cases")
    private final List<SQLNodeConverterTestCase> testCases = new LinkedList<>();
    
    /**
     * Get all SQL node converter test cases.
     *
     * @return SQL node converter test cases
     */
    public Map<String, SQLNodeConverterTestCase> getTestCases() {
        Map<String, SQLNodeConverterTestCase> result = new HashMap<>(testCases.size(), 1F);
        for (SQLNodeConverterTestCase each : testCases) {
            Collection<String> databaseTypes = getDatabaseTypes(each);
            for (String sqlCaseType : getSQLCaseTypes(each)) {
                for (String databaseType : databaseTypes) {
                    result.put(each.getSqlCaseId() + "_" + sqlCaseType + "_" + databaseType, each);
                }
            }
        }
        return result;
    }
    
    private static Collection<String> getDatabaseTypes(final SQLNodeConverterTestCase each) {
        return Strings.isNullOrEmpty(each.getDatabaseTypes()) ? Arrays.asList("MySQL", "PostgreSQL", "openGauss", "Oracle", "SQLServer")
                : Splitter.on(",").trimResults().splitToList(each.getDatabaseTypes());
    }
    
    private static Collection<String> getSQLCaseTypes(final SQLNodeConverterTestCase each) {
        return Strings.isNullOrEmpty(each.getSqlCaseTypes()) ? Arrays.stream(SQLCaseType.values()).map(Enum::name).collect(Collectors.toList())
                : Splitter.on(",").trimResults().splitToList(each.getSqlCaseTypes());
    }
}
