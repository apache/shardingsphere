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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.util.AlgorithmValueUtils;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Properties;

/**
 * MD5 message digest engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MD5MessageDigestEngine {
    
    private static final String MD5 = "MD5";
    
    private static final String SALT = "salt";
    
    /**
     * Digest value with MD5.
     *
     * @param plainValue plain value
     * @param props algorithm properties
     * @return digest bytes
     * @throws GeneralSecurityException digest failure
     */
    public static byte[] digest(final Object plainValue, final Properties props) throws GeneralSecurityException {
        return digest(plainValue, props.getProperty(SALT, ""));
    }
    
    /**
     * Digest value with MD5.
     *
     * @param plainValue plain value
     * @param salt salt
     * @return digest bytes
     * @throws GeneralSecurityException digest failure
     */
    public static byte[] digest(final Object plainValue, final String salt) throws GeneralSecurityException {
        return MessageDigest.getInstance(MD5).digest(AlgorithmValueUtils.convertToBytes(AlgorithmValueUtils.convertToUTF8Text(plainValue) + salt, null));
    }
}
