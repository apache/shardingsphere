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

package org.apache.shardingsphere.dialect.postgresql.message;

import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.junit.Assert;
import org.junit.Test;
import org.postgresql.util.ServerErrorMessage;

import static org.hamcrest.CoreMatchers.is;

public final class ServerErrorMessageBuilderTest {
    
    @Test
    public void assertToServerErrorMessage() {
        ServerErrorMessage actual = ServerErrorMessageBuilder.build("FATAL", PostgreSQLVendorError.SYSTEM_ERROR, "foo_reason");
        Assert.assertThat(actual.getSeverity(), is("FATAL"));
        Assert.assertThat(actual.getSQLState(), is(PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue()));
        Assert.assertThat(actual.getMessage(), is("foo_reason"));
    }
}
