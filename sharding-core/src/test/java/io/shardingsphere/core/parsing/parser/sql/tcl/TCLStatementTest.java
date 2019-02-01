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

package io.shardingsphere.core.parsing.parser.sql.tcl;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.sql.statement.tcl.TCLStatement;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.LexerEngineFactory;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class TCLStatementTest {
    
    @Test
    public void assertIsTCLForSetTransaction() {
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE");
        lexerEngine.nextToken();
        assertTrue(TCLStatement.isTCLUnsafe(DatabaseType.MySQL, DefaultKeyword.SET, lexerEngine));
    }
    
    @Test
    public void assertIsTCLForSetAutoCommit() {
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, "SET AUTOCOMMIT = 0");
        lexerEngine.nextToken();
        assertTrue(TCLStatement.isTCLUnsafe(DatabaseType.MySQL, DefaultKeyword.SET, lexerEngine));
    }
    
    @Test
    public void assertIsTCLForCommit() {
        assertTrue(TCLStatement.isTCL(DefaultKeyword.COMMIT));
    }
    
    @Test
    public void assertIsTCLForRollback() {
        assertTrue(TCLStatement.isTCL(DefaultKeyword.ROLLBACK));
    }
    
    @Test
    public void assertIsTCLForSavePoint() {
        assertTrue(TCLStatement.isTCL(DefaultKeyword.SAVEPOINT));
    }
    
    @Test
    public void assertIsTCLForBegin() {
        assertTrue(TCLStatement.isTCL(DefaultKeyword.BEGIN));
    }
    
    @Test
    public void assertIsNotTCL() {
        assertFalse(TCLStatement.isTCL(DefaultKeyword.SELECT));
    }
}
