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

package org.apache.shardingsphere.distsql.parser.rdl;

import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class RegisterStorageUnitTest {
    
    @Test
    void assertRegisterStorageUnitSpecialCharacters() {
        String sql = "REGISTER STORAGE UNIT test_db ("
                + "URL='jdbc:mysql://127.0.0.1:3306/test_db',"
                + "USER='root',PASSWORD='\\'\\\"r\\[oo]t');";
        RegisterStorageUnitStatement distSQLStatement = getRegisterStorageUnitStatement(sql);
        URLBasedDataSourceSegment sqlSegment = (URLBasedDataSourceSegment) distSQLStatement.getStorageUnits().iterator().next();
        assertThat(sqlSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test_db"));
        assertThat(sqlSegment.getName(), is("test_db"));
        assertThat(sqlSegment.getUser(), is("root"));
        assertThat(sqlSegment.getPassword(), is("'\"r\\[oo]t"));
    }
    
    private RegisterStorageUnitStatement getRegisterStorageUnitStatement(final String sql) {
        DistSQLStatementParserEngine distSQLStatementParserEngine = new DistSQLStatementParserEngine();
        return (RegisterStorageUnitStatement) distSQLStatementParserEngine.parse(sql);
    }
}
