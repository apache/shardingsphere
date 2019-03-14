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

package org.apache.shardingsphere.core.parse.parser.sql.dal;

import org.apache.shardingsphere.core.parse.lexer.dialect.mysql.MySQLKeyword;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DALStatementTest {
    
    @Test
    public void assertIsDALForUse() {
        assertTrue(DALStatement.isDAL(DefaultKeyword.USE));
    }
    
    @Test
    public void assertIsDALForDesc() {
        assertTrue(DALStatement.isDAL(DefaultKeyword.DESC));
    }
    
    @Test
    public void assertIsDALForDescribe() {
        assertTrue(DALStatement.isDAL(MySQLKeyword.DESCRIBE));
    }
    
    @Test
    public void assertIsDALForShow() {
        assertTrue(DALStatement.isDAL(MySQLKeyword.SHOW));
    }
    
    @Test
    public void assertIsNotDAL() {
        assertFalse(DALStatement.isDAL(DefaultKeyword.FROM));
    }
}
