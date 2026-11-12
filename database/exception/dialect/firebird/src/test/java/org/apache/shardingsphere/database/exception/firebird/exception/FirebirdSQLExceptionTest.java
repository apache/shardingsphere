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

package org.apache.shardingsphere.database.exception.firebird.exception;

import org.apache.shardingsphere.database.exception.firebird.vendor.FirebirdVendorError;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdSQLExceptionTest {
    
    @Test
    void assertConstruct() {
        FirebirdSQLException actual = new FirebirdSQLException(FirebirdVendorError.INVALID_BATCH_PARAMETER_VERSION, 2, 1);
        assertThat(actual.getMessage(), is("Wrong version of batch parameters block 2, should be 1"));
        assertThat(actual.getSQLState(), is("22000"));
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_batch_param_version));
        assertThat(actual.getMessageArguments(), is(Arrays.<Object>asList(2, 1)));
    }
}
