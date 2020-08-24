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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLServerErrorCodeTest {
    
    @Test
    public void assertAccessDeniedError() {
        assertThat(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode(), is(1045));
        assertThat(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState(), is("28000"));
        assertThat(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), is("Access denied for user '%s'@'%s' (using password: %s)"));
    }
    
    @Test
    public void assertBadDbError() {
        assertThat(MySQLServerErrorCode.ER_BAD_DB_ERROR.getErrorCode(), is(1049));
        assertThat(MySQLServerErrorCode.ER_BAD_DB_ERROR.getSqlState(), is("42000"));
        assertThat(MySQLServerErrorCode.ER_BAD_DB_ERROR.getErrorMessage(), is("Unknown database '%s'"));
    }
    
    @Test
    public void assertErrorOnModifyingGtidExecutedTable() {
        assertThat(MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getErrorCode(), is(3176));
        assertThat(MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getSqlState(), is("HY000"));
        assertThat(MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getErrorMessage(), is("Please do not modify the %s table with an XA transaction. "
                + "This is an internal system table used to store GTIDs for committed transactions. "
                + "Although modifying it can lead to an inconsistent GTID state, if neccessary you can modify it with a non-XA transaction."));
    }
}
