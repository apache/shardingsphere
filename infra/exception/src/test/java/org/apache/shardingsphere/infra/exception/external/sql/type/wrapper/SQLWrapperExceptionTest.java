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

package org.apache.shardingsphere.infra.exception.external.sql.type.wrapper;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class SQLWrapperExceptionTest {
    
    @Test
    void assertToSQLException() {
        SQLException actual = new SQLWrapperException(new SQLException("reason", "1", 10)).toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorCode(), is(30005));
        assertThat(actual.getMessage(), startsWith("Underlying SQL state: 1, underlying error code: 10."));
    }
}
