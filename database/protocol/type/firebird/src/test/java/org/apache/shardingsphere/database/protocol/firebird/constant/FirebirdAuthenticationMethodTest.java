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

package org.apache.shardingsphere.database.protocol.firebird.constant;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdAuthenticationMethodTest {
    
    @Test
    void assertMethodNamesAndHashAlgorithms() {
        assertThat(FirebirdAuthenticationMethod.SRP.getMethodName(), is("Srp"));
        assertThat(FirebirdAuthenticationMethod.SRP.getHashAlgorithm(), is("SHA-1"));
        assertThat(FirebirdAuthenticationMethod.SRP224.getMethodName(), is("Srp224"));
        assertThat(FirebirdAuthenticationMethod.SRP224.getHashAlgorithm(), is("SHA-224"));
        assertThat(FirebirdAuthenticationMethod.SRP256.getMethodName(), is("Srp256"));
        assertThat(FirebirdAuthenticationMethod.SRP256.getHashAlgorithm(), is("SHA-256"));
        assertThat(FirebirdAuthenticationMethod.SRP384.getMethodName(), is("Srp384"));
        assertThat(FirebirdAuthenticationMethod.SRP384.getHashAlgorithm(), is("SHA-384"));
        assertThat(FirebirdAuthenticationMethod.SRP512.getMethodName(), is("Srp512"));
        assertThat(FirebirdAuthenticationMethod.SRP512.getHashAlgorithm(), is("SHA-512"));
        assertThat(FirebirdAuthenticationMethod.LEGACY_AUTH.getMethodName(), is("Legacy_Auth"));
        assertThat(FirebirdAuthenticationMethod.LEGACY_AUTH.getHashAlgorithm(), is((String) null));
    }
}
