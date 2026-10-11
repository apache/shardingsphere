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

package org.apache.shardingsphere.infra.algorithm.messagedigest.md5;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MD5MessageDigestEngineTest {
    
    @Test
    void assertDigest() throws GeneralSecurityException {
        assertThat(Hex.encodeHexString(MD5MessageDigestEngine.digest("test", "salt")), is("315240c61218a4a861ec949166a85ef0"));
    }
    
    @Test
    void assertDigestWithProperties() throws GeneralSecurityException {
        Properties props = new Properties();
        props.setProperty("salt", "salt");
        assertThat(Hex.encodeHexString(MD5MessageDigestEngine.digest("test", props)), is("315240c61218a4a861ec949166a85ef0"));
    }
}
