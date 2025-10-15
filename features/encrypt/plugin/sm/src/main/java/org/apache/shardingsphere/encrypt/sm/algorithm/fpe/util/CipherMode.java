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

package org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util;

import javax.crypto.Cipher;

/**
 * Cipher模式的枚举封装
 *
 * @author looly
 * @since 5.4.3
 */
public enum CipherMode {
    
    /**
     * 加密模式
     */
    encrypt(Cipher.ENCRYPT_MODE),
    /**
     * 解密模式
     */
    decrypt(Cipher.DECRYPT_MODE),
    /**
     * 包装模式
     */
    wrap(Cipher.WRAP_MODE),
    /**
     * 拆包模式
     */
    unwrap(Cipher.UNWRAP_MODE);
    
    /**
     * 构造
     *
     * @param value 见{@link Cipher}
     */
    CipherMode(int value) {
        this.value = value;
    }
    
    private final int value;
    
    /**
     * 获取枚举值对应的int表示
     *
     * @return 枚举值对应的int表示
     */
    public int getValue() {
        return this.value;
    }
}
