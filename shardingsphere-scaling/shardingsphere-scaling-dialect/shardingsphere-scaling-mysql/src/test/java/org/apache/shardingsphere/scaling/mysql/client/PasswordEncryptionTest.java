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

import lombok.SneakyThrows;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PasswordEncryptionTest {
    
    @Test
    public void assertEncryptWithMySQL41() throws NoSuchAlgorithmException {
        byte[] passwordBytes = "password".getBytes();
        byte[] seed = getRandomSeed();
        assertThat(PasswordEncryption.encryptWithMySQL41(passwordBytes, seed), is(getMySQL41ExpectedPassword()));
    }
    
    private byte[] getMySQL41ExpectedPassword() {
        return new byte[]{-110, -31, 48, -32, -22, -29, 54, -40, 54, 118, -119, -16, -96, -25, 121, -64, -75, -103, 73, -44};
    }
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    @Test
    public void encryptEncryptWithSha2() {
        assertThat(PasswordEncryption.encryptWithSha2("123456".getBytes(), getRandomSeed()), is(getSha2ExpectedPassword()));
    }
    
    private byte[] getSha2ExpectedPassword() {
        return new byte[]{-47, -106, -46, 74, 24, 12, 49, 33, 47, -65, -43, -23, -43, 4, -107, 103, -4, 63, -88, 67, 118, 29, -4, -9, 15, -123, 94, -116, 106, -121, 11, 29};
    }
    
    private byte[] getRandomSeed() {
        byte[] result = new byte[20];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) i;
        }
        return result;
    }
    
    @Test
    public void assertEncryptWithRSAPublicKey() {
        PasswordEncryption.encryptWithRSAPublicKey("123456", getRandomSeed(),
                "RSA/ECB/OAEPWithSHA-1AndMGF1Padding",
                mockPublicKey());
    }
    
    private String mockPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n"
                + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1ealW/qDdArgzCMnE5Cz\n"
                + "6FHskcTTweMncG6A124rn2DFBZvmZNyTBiFLM7Scp3jFSyqpw2xg6aaKcM9eCaCf\n"
                + "nJg4A18HgpAxrFnijVADgsNrHlSniNe2AsN+/uLpEtWezVLr823WvPLgMKQMRWfy\n"
                + "UD24rpoC2Leir+rvyG8xbDHX65NPGxPFGrlwo7kbUqrgQlYOC3x64C4/S/6K6EZQ\n"
                + "XaUZwZHdXjEme0/D8p8KBXdMipanZXwHdL+LOBSACj3/FwHn+6oZO2k02g80uofs\n"
                + "zFdWMjpPVqVCqe85GRFzEY73wDYEItl0d+9a9OV3FFZqVgC2FLk3cD5qajPtyo6v\n"
                + "UQIDAQAB\n"
                + "-----END PUBLIC KEY-----";
    }
}
