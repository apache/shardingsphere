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

package io.shardingsphere.shardingproxy.transport.mysql.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ServerErrorCodeTest {
    
    @Test
    public void assertAccessDeniedError() {
        assertThat(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode(), is(1045));
        assertThat(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState(), is("28000"));
        assertThat(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), is("Access denied for user '%s'@'%s' (using password: %s)"));
    }
    
    @Test
    public void assertBadDbError() {
        assertThat(ServerErrorCode.ER_BAD_DB_ERROR.getErrorCode(), is(1049));
        assertThat(ServerErrorCode.ER_BAD_DB_ERROR.getSqlState(), is("42000"));
        assertThat(ServerErrorCode.ER_BAD_DB_ERROR.getErrorMessage(), is("Unknown database '%s'"));
    }
    
    @Test
    public void assertErrorOnModifyingGtidExecutedTable() {
        assertThat(ServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getErrorCode(), is(3176));
        assertThat(ServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getSqlState(), is("HY000"));
        assertThat(ServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE.getErrorMessage(), is("Please do not modify the %s table with an XA transaction. "
                + "This is an internal system table used to store GTIDs for committed transactions. "
                + "Although modifying it can lead to an inconsistent GTID state, if neccessary you can modify it with a non-XA transaction."));
    }
    
    @Test
    public void assertStdUnknownException() {
        assertThat(ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION.getErrorCode(), is(3054));
        assertThat(ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION.getSqlState(), is("HY000"));
        assertThat(ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION.getErrorMessage(), is("Unknown exception: %s"));
    }
    
    @Test
    public void assertUnsupportedCommand() {
        assertThat(ServerErrorCode.ER_UNSUPPORTED_COMMAND.getErrorCode(), is(9999));
        assertThat(ServerErrorCode.ER_UNSUPPORTED_COMMAND.getSqlState(), is("X9999"));
        assertThat(ServerErrorCode.ER_UNSUPPORTED_COMMAND.getErrorMessage(), is("Unsupported command packet: '%s'"));
    }
}
