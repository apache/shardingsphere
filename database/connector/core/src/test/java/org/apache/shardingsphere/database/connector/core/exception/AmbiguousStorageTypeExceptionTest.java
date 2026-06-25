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

package org.apache.shardingsphere.database.connector.core.exception;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AmbiguousStorageTypeExceptionTest {
    
    @Test
    void assertToSQLException() {
        SQLException actual = new AmbiguousStorageTypeException("jdbc:mysql://localhost:3306/foo_db", Arrays.asList("Doris", "MariaDB")).toSQLException();
        assertThat(actual.getSQLState(), is(XOpenSQLState.CONNECTION_EXCEPTION.getValue()));
        assertThat(actual.getErrorCode(), is(13102));
        assertThat(actual.getMessage(), is("Ambiguous storage type of URL 'jdbc:mysql://localhost:3306/foo_db', matched database types are Doris, MariaDB."));
    }
}
