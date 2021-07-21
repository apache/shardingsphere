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

package org.apache.shardingsphere.infra.security;

import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

/**
 * Typed of Secure.
 */
public interface AlgorithmSecure extends TypedSPI {
    
    /**
     * encrypt input.
     *
     * @param input the input password
     * @param charsetName the password' charset
     * @return String the encrypted password
     * @throws Exception when encrypt failed throw this exception
     */
    default String encrypt(final String input, final String charsetName) throws Exception {
        return encrypt(input.getBytes(charsetName));
    }

    /**
     * encrypt input.
     *
     * @param input the input password
     * @return String the encrypted password
     * @throws Exception when encrypt failed throw this exception
     */
    default String encrypt(final String input) throws Exception {
        return encrypt(input, "UTF-8");
    }

    /**
     * encrypt input.
     *
     * @param content the password byte array
     * @return String the encrypted password
     * @throws Exception when encrypt failed throw this exception
     */
    String encrypt(byte[] content) throws Exception;

    /**
     * decrypt input.
     * @param input the encrypt returned result
     * @param charsetName the decrypted password's charsetName
     * @return String the decrypted password
     * @throws Exception when decrypt failed throw this exception
     */
    String decrypt(String input, String charsetName) throws Exception;

    /**
     * decrypt input.
     * @param input the encrypt returned result
     * @return String the decrypted password
     * @throws Exception when decrypt failed throw this exception
     */
    default String decrypt(final String input) throws Exception {
        return decrypt(input, "UTF-8");
    }
}
