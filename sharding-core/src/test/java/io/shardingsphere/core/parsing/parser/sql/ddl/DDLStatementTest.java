/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.sql.ddl;

import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DDLStatementTest {
    
    @Test
    public void assertIsDDLForCreateTable() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.CREATE, DefaultKeyword.TABLE));
    }
    
    @Test
    public void assertIsDDLForCreateIndex() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.CREATE, DefaultKeyword.INDEX));
    }
    
    @Test
    public void assertIsDDLForCreateUniqueIndex() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.CREATE, DefaultKeyword.UNIQUE));
    }
    
    @Test
    public void assertIsDDLForAlterTable() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.ALTER, DefaultKeyword.TABLE));
    }
    
    @Test
    public void assertIsDDLForDropTable() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.DROP, DefaultKeyword.TABLE));
    }
    
    @Test
    public void assertIsDDLForDropIndex() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.DROP, DefaultKeyword.INDEX));
    }
    
    @Test
    public void assertIsDDLForTruncateTable() {
        assertTrue(DDLStatement.isDDL(DefaultKeyword.TRUNCATE, DefaultKeyword.TABLE));
    }
    
    @Test
    public void assertIsNotDDLForCreateLogin() {
        assertFalse(DDLStatement.isDDL(DefaultKeyword.CREATE, DefaultKeyword.LOGIN));
    }
    
    @Test
    public void assertIsNotDDLForCreateUser() {
        assertFalse(DDLStatement.isDDL(DefaultKeyword.CREATE, DefaultKeyword.USER));
    }
    
    @Test
    public void assertIsNotDDLForCreateRole() {
        assertFalse(DDLStatement.isDDL(DefaultKeyword.CREATE, DefaultKeyword.ROLE));
    }
}
