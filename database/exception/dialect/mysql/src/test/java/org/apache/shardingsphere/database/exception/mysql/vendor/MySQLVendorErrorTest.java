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

package org.apache.shardingsphere.database.exception.mysql.vendor;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLVendorErrorTest {
    
    @Test
    void assertAccessDeniedError() {
        assertThat(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getVendorCode(), is(1045));
        assertThat(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getSqlState().getValue(), is("28000"));
        assertThat(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getReason(), is("Access denied for user '%s'@'%s' (using password: %s)"));
    }
    
    @Test
    void assertBadDbError() {
        assertThat(MySQLVendorError.ER_BAD_DB_ERROR.getVendorCode(), is(1049));
        assertThat(MySQLVendorError.ER_BAD_DB_ERROR.getSqlState().getValue(), is("42000"));
        assertThat(MySQLVendorError.ER_BAD_DB_ERROR.getReason(), is("Unknown database '%s'"));
    }
    
    @Test
    void assertErrorOnModifyingGtidExecutedTable() {
        assertThat(MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getVendorCode(), is(3176));
        assertThat(MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getSqlState().getValue(), is("HY000"));
        assertThat(MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getReason(), is("Please do not modify the %s table with an XA transaction. "
                + "This is an internal system table used to store GTIDs for committed transactions. "
                + "Although modifying it can lead to an inconsistent GTID state, if necessary you can modify it with a non-XA transaction."));
    }
}
