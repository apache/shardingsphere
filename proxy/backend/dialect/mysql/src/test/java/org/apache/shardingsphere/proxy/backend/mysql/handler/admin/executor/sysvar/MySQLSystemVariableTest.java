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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar;

import org.apache.shardingsphere.database.exception.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorLocalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.IncorrectGlobalLocalVariableException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MySQLSystemVariableTest {
    
    @Test
    void assertFindSystemVariable() {
        assertTrue(MySQLSystemVariable.findSystemVariable("max_connections").isPresent());
        assertFalse(MySQLSystemVariable.findSystemVariable("non_exist").isPresent());
    }
    
    @Test
    void assertGetValueForGlobalScope() {
        assertThat(MySQLSystemVariable.ACTIVATE_ALL_ROLES_ON_LOGIN.getValue(MySQLSystemVariableScope.GLOBAL, null), is("0"));
    }
    
    @Test
    void assertGetValueRejectsGlobalForOnlySessionVariable() {
        assertThrows(IncorrectGlobalLocalVariableException.class, () -> MySQLSystemVariable.RAND_SEED1.getValue(MySQLSystemVariableScope.GLOBAL, null));
    }
    
    @Test
    void assertGetValueForSessionScope() {
        assertThat(MySQLSystemVariable.AUTOCOMMIT.getValue(MySQLSystemVariableScope.SESSION, mock()), is("1"));
    }
    
    @Test
    void assertGetValueRejectsSessionForGlobalOnlyVariable() {
        assertThrows(IncorrectGlobalLocalVariableException.class, () -> MySQLSystemVariable.ACTIVATE_ALL_ROLES_ON_LOGIN.getValue(MySQLSystemVariableScope.SESSION, mock()));
    }
    
    @Test
    void assertValidateSetTargetScopeRejectsGlobalForOnlySessionVariable() {
        assertThrows(ErrorLocalVariableException.class, () -> MySQLSystemVariable.RAND_SEED1.validateSetTargetScope(MySQLSystemVariableScope.GLOBAL));
    }
    
    @Test
    void assertValidateSetTargetScopeRejectsSessionForGlobalVariable() {
        assertThrows(ErrorGlobalVariableException.class, () -> MySQLSystemVariable.ADMIN_ADDRESS.validateSetTargetScope(MySQLSystemVariableScope.SESSION));
    }
    
    @Test
    void assertValidateSetTargetScopePassesForAllowedScope() {
        assertDoesNotThrow(() -> MySQLSystemVariable.AUTOCOMMIT.validateSetTargetScope(MySQLSystemVariableScope.SESSION));
    }
    
    @Test
    void assertValidateSetTargetScopePassesForGlobalScope() {
        assertDoesNotThrow(() -> MySQLSystemVariable.ADMIN_ADDRESS.validateSetTargetScope(MySQLSystemVariableScope.GLOBAL));
    }
}
