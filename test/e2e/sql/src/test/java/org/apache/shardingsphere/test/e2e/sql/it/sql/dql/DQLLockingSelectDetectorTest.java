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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dql;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DQLLockingSelectDetectorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("lockingSelectProvider")
    void assertLockingSelect(final String name, final String sql) {
        assertTrue(DQLLockingSelectDetector.containsLockingSelect(sql));
    }
    
    private static Stream<Arguments> lockingSelectProvider() {
        return Stream.of(
                Arguments.of("For update", "SELECT * FROM t_order WHERE order_id = 1 FOR UPDATE"),
                Arguments.of("For no key update", "SELECT * FROM t_order FOR NO KEY UPDATE"),
                Arguments.of("For share", "SELECT * FROM t_order FOR SHARE"),
                Arguments.of("For key share", "SELECT * FROM t_order FOR KEY SHARE"),
                Arguments.of("Lock in share mode", "SELECT * FROM t_order LOCK IN SHARE MODE"),
                Arguments.of("SQL Server updlock", "SELECT * FROM t_order WITH (UPDLOCK) WHERE order_id = 1"),
                Arguments.of("SQL Server holdlock", "SELECT * FROM t_order WITH (ROWLOCK, HOLDLOCK)"),
                Arguments.of("SQL Server xlock", "SELECT * FROM t_order WITH (XLOCK, TABLOCKX)"),
                Arguments.of("DB2 keep update locks", "SELECT * FROM t_order WITH RS USE AND KEEP UPDATE LOCKS"),
                Arguments.of("DB2 keep share locks", "SELECT * FROM t_order WITH CS USE AND KEEP SHARE LOCKS"),
                Arguments.of("With lock", "SELECT * FROM t_order WITH LOCK"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonLockingSelectProvider")
    void assertNonLockingSelect(final String name, final String sql) {
        assertFalse(DQLLockingSelectDetector.containsLockingSelect(sql));
    }
    
    private static Stream<Arguments> nonLockingSelectProvider() {
        return Stream.of(
                Arguments.of("Plain select", "SELECT * FROM t_order"),
                Arguments.of("For update literal", "SELECT 'FOR UPDATE' AS content FROM t_order"),
                Arguments.of("For update comment", "SELECT * FROM t_order -- FOR UPDATE"),
                Arguments.of("For update block comment", "SELECT * FROM t_order /* FOR UPDATE */"),
                Arguments.of("For update quoted identifier", "SELECT \"FOR UPDATE\" FROM t_order"),
                Arguments.of("Non select DML", "UPDATE t_order SET status = 'FOR UPDATE' WHERE order_id = 1"),
                Arguments.of("No lock hint", "SELECT * FROM t_order WITH (NOLOCK)"),
                Arguments.of("For read only", "SELECT * FROM t_order FOR READ ONLY"),
                Arguments.of("CTE named lock", "WITH lock AS (SELECT * FROM t_order) SELECT * FROM lock"));
    }
}
