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

public class FPEConstants {
    
    public static final String NUMBER_CHARACTERSET = "0123456789";
    public static final String ENGLISH_LOWERCASE_CHARACTERSET = "abcdefghijklmnopqrstuvwxyz";
    public static final String ENGLISH_UPPERCASE_CHARACTERSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String CHINESE_CHARACTERSET = "";
    public static final String SPECIAL_CHARACTERSET = "·!#$%&()*+,-./:;<=>@[\\]^_{|}~";
    
    /**
     * fpe加密字符集
     */
    public static enum FPEEncryptCharacterSet {
        
        NUMBER(1, NUMBER_CHARACTERSET, "纯数字集"), LOWERCASE(2, ENGLISH_LOWERCASE_CHARACTERSET, "英文小写字符集"), UPPERCASE(3, ENGLISH_UPPERCASE_CHARACTERSET, "英文大写字符集"),
        CHINESE(4, CHINESE_CHARACTERSET, "中文字符集"), SPECIAL(5, SPECIAL_CHARACTERSET, "特殊字符集");
        
        private FPEEncryptCharacterSet(Integer value, String characterSet, String name) {
            this.value = value;
            this.characterSet = characterSet;
            this.name = name;
        }
        
        private final Integer value;
        private final String name;
        private final String characterSet;
        
        public Integer getValue() {
            return value;
        }
        
        public String getName() {
            return name;
        }
        
        public String getCharacterSet() {
            return characterSet;
        }
        
        public static String getFPEEncryptCharacterSet(Integer value) {
            for (FPEEncryptCharacterSet characterSet : values()) {
                if (characterSet.getValue() == value) {
                    if (value == CHINESE.value) {
                        return getCommonChineseStr();
                    }
                    // 获取指定的枚举
                    return characterSet.getCharacterSet();
                }
            }
            return null;
        }
        
    }
    
    public void initParam() {
        this.CHINESE_CHARACTERSET = getCommonChineseStr();
    }
    
    public static String getCommonChineseStr() {
        int start = 0x4E00; // 中文字符的起始编码
        int end = 0x9FA5; // 中文字符的结束编码
        StringBuffer sb = new StringBuffer();
        for (int code = start; code <= end; code++) {
            String chineseChar = String.valueOf((char) code);
            sb.append(chineseChar);
        }
        return sb.toString();
    }
    
}
