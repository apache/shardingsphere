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

/**
 * 对称算法类型<br>
 * see: https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyGenerator
 *
 * @author Looly
 */
public enum SymmetricAlgorithm {
    
    /**
     * 默认的AES加密方式：AES/ECB/PKCS5Padding
     */
    AES("AES"),
    ARCFOUR("ARCFOUR"),
    Blowfish("Blowfish"),
    /**
     * 默认的DES加密方式：DES/ECB/PKCS5Padding
     */
    DES("DES"),
    /**
     * 3DES算法，默认实现为：DESede/CBC/PKCS5Padding
     */
    DESede("DESede"),
    RC2("RC2"),
    
    PBEWithMD5AndDES("PBEWithMD5AndDES"),
    PBEWithSHA1AndDESede("PBEWithSHA1AndDESede"),
    PBEWithSHA1AndRC2_40("PBEWithSHA1AndRC2_40");
    
    private final String value;
    
    /**
     * 构造
     *
     * @param value 算法的字符串表示，区分大小写
     */
    SymmetricAlgorithm(String value) {
        this.value = value;
    }
    
    /**
     * 获得算法的字符串表示形式
     *
     * @return 算法字符串
     */
    public String getValue() {
        return this.value;
    }
}