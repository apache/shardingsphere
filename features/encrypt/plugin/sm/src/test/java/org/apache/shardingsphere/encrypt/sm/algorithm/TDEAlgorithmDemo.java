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

package org.apache.shardingsphere.encrypt.sm.algorithm;

import org.apache.shardingsphere.encrypt.api.context.EncryptContext;

import java.util.Properties;

/**
 * TDE算法验证演示类
 * 用于验证TDE算法的基本功能
 */
public final class TDEAlgorithmDemo {
    
    public static void main(String[] args) {
        // 创建算法实例
        TDEEncryptAlgorithm algorithm = new TDEEncryptAlgorithm();
        
        // 创建加密上下文
        EncryptContext context = new EncryptContext("test_db", "test_schema", "test_table", "test_column");
        
        // 测试数字加密（手机号）
        testNumberEncryption(algorithm, context);
        
        // 测试混合字符加密
        testMixedCharacterEncryption(algorithm, context);
        
        System.out.println("TDE算法验证完成！");
    }
    
    private static void testNumberEncryption(TDEEncryptAlgorithm algorithm, EncryptContext context) {
        System.out.println("=== 测试数字加密（手机号）===");
        
        // 配置数字字符集
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1"); // 1=数字字符集
        algorithm.init(props);
        
        String phoneNumber = "13812345678";
        String encrypted = algorithm.encrypt(phoneNumber, context);
        String decrypted = (String) algorithm.decrypt(encrypted, context);
        
        System.out.println("原始手机号: " + phoneNumber);
        System.out.println("加密结果:   " + encrypted);
        System.out.println("解密结果:   " + decrypted);
        System.out.println("长度保持:   " + (phoneNumber.length() == encrypted.length()));
        System.out.println("加解密正确: " + phoneNumber.equals(decrypted));
        System.out.println();
    }
    
    private static void testMixedCharacterEncryption(TDEEncryptAlgorithm algorithm, EncryptContext context) {
        System.out.println("=== 测试混合字符加密 ===");
        
        // 配置混合字符集（数字+小写字母+大写字母）
        Properties props = new Properties();
        props.setProperty("fpe-key-value", "MySecretKey12345");
        props.setProperty("fpe-number-mapper", "1,2,3"); // 1=数字, 2=小写字母, 3=大写字母
        algorithm.init(props);
        
        String testData = "Test123";
        String encrypted = algorithm.encrypt(testData, context);
        String decrypted = (String) algorithm.decrypt(encrypted, context);
        
        System.out.println("原始数据: " + testData);
        System.out.println("加密结果: " + encrypted);
        System.out.println("解密结果: " + decrypted);
        System.out.println("长度保持: " + (testData.length() == encrypted.length()));
        System.out.println("加解密正确: " + testData.equals(decrypted));
        System.out.println();
    }
}