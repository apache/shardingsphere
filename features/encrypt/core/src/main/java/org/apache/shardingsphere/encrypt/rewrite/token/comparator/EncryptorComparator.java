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

package org.apache.shardingsphere.encrypt.rewrite.token.comparator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;

/**
 * Encryptor comparator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptorComparator {
    
    /**
     * Compare whether same encryptor.
     *
     * @param encryptor1 encryptor 1 to be compared
     * @param encryptor2 encryptor 2 to be compared
     * @return same encryptors or not
     */
    public static boolean isSame(final EncryptAlgorithm encryptor1, final EncryptAlgorithm encryptor2) {
        return null != encryptor1 && null != encryptor2 ? encryptor1.toConfiguration().equals(encryptor2.toConfiguration()) : encryptor1 == encryptor2;
    }
}
