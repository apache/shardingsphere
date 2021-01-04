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

package org.apache.shardingsphere.scaling.mysql.client;

import org.junit.Test;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLPasswordEncryptorTest {
    
    @Test
    public void assertEncryptWithMySQL41() throws NoSuchAlgorithmException {
        byte[] passwordBytes = "password".getBytes();
        byte[] seed = getRandomSeed();
        assertThat(MySQLPasswordEncryptor.encryptWithMySQL41(passwordBytes, seed), is(getExpectedPassword()));
    }
    
    private byte[] getRandomSeed() {
        byte[] result = new byte[20];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) i;
        }
        return result;
    }
    
    private byte[] getExpectedPassword() {
        return new byte[] {-110, -31, 48, -32, -22, -29, 54, -40, 54, 118, -119, -16, -96, -25, 121, -64, -75, -103, 73, -44};
    }
}
