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

package org.apache.shardingsphere.database.connector.h2.checker;

import org.apache.shardingsphere.database.connector.core.checker.DialectDatabasePrivilegeChecker;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class H2DatabasePrivilegeCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final DialectDatabasePrivilegeChecker checker = DatabaseTypedSPILoader.getService(DialectDatabasePrivilegeChecker.class, databaseType);
    
    @Test
    void assertCheck() {
        assertDoesNotThrow(() -> checker.check(mock(DataSource.class), PrivilegeCheckType.SELECT));
    }
}
