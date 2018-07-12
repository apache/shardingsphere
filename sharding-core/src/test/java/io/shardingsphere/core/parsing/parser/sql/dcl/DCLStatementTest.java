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

package io.shardingsphere.core.parsing.parser.sql.dcl;

import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DCLStatementTest {
    
    @Test
    public void assertIsDCLForGrant() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.GRANT, DefaultKeyword.SELECT));
    }
    
    @Test
    public void assertIsDCLForRevoke() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.REVOKE, DefaultKeyword.SELECT));
    }
    
    @Test
    public void assertIsDCLForDeny() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.DENY, DefaultKeyword.SELECT));
    }
    
    @Test
    public void assertIsDCLForCreateLogin() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.CREATE, DefaultKeyword.LOGIN));
    }
    
    @Test
    public void assertIsDCLForCreateUser() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.CREATE, DefaultKeyword.USER));
    }
    
    @Test
    public void assertIsDCLForCreateRole() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.CREATE, DefaultKeyword.ROLE));
    }
    
    @Test
    public void assertIsDCLForAlterLogin() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.ALTER, DefaultKeyword.LOGIN));
    }
    
    @Test
    public void assertIsDCLForAlterUser() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.ALTER, DefaultKeyword.USER));
    }
    
    @Test
    public void assertIsDCLForAlterRole() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.ALTER, DefaultKeyword.ROLE));
    }
    
    @Test
    public void assertIsDCLForDropLogin() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.DROP, DefaultKeyword.LOGIN));
    }
    
    @Test
    public void assertIsDCLForDropUser() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.DROP, DefaultKeyword.USER));
    }
    
    @Test
    public void assertIsDCLForDropRole() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.DROP, DefaultKeyword.ROLE));
    }
    
    @Test
    public void assertIsDCLForRenameLogin() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.RENAME, DefaultKeyword.LOGIN));
    }
    
    @Test
    public void assertIsDCLForRenameUser() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.RENAME, DefaultKeyword.USER));
    }
    
    @Test
    public void assertIsDCLForRenameRole() {
        assertTrue(DCLStatement.isDCL(DefaultKeyword.RENAME, DefaultKeyword.ROLE));
    }
    
    @Test
    public void assertIsNotDCLForCreateTable() {
        assertFalse(DCLStatement.isDCL(DefaultKeyword.CREATE, DefaultKeyword.TABLE));
    }
}
