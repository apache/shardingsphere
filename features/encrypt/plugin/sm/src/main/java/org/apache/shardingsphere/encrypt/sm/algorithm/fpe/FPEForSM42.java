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

package org.apache.shardingsphere.encrypt.sm.algorithm.fpe;

import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.AES;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.KeyUtil;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.Padding;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.SM4;
import org.bouncycastle.crypto.AlphabetMapper;
import org.bouncycastle.jcajce.spec.FPEParameterSpec;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FPEForSM42 {
    
    // 映射字符表，规定了明文和密文的字符范围
    private final AES aes;
    
    private final SM4 sm4;
    private final AlphabetMapper mapper;
    
    private static final double TWO_TO_96 = Math.pow(2, 96);
    
    private int sortCode = 0;
    
    private String numberMapperStr;
    /**
     * 构造，使用空的Tweak
     *
     * @param mode   FPE模式枚举，可选FF1或FF3-1
     * @param key    密钥，{@code null}表示随机密钥，长度必须是16bit、24bit或32bit
     * @param mapper Alphabet字典映射，被加密的字符范围和这个映射必须一致，例如手机号、银行卡号等字段可以采用数字字母字典表
     */
    public FPEForSM42(FPEMode mode, byte[] key, AlphabetMapper mapper) {
        this(mode, key, mapper, null);
    }
    
    /**
     * 构造
     *
     * @param mode   FPE模式枚举，可选FF1或FF3-1
     * @param key    密钥，{@code null}表示随机密钥，长度必须是16bit、24bit或32bit
     * @param mapper Alphabet字典映射，被加密的字符范围和这个映射必须一致，例如手机号、银行卡号等字段可以采用数字字母字典表
     * @param tweak  Tweak是为了解决因局部加密而导致结果冲突问题，通常情况下将数据的不可变部分作为Tweak，{@code null}使用默认长度全是0的bytes
     */
    public FPEForSM42(FPEMode mode, byte[] key, AlphabetMapper mapper, byte[] tweak) {
        
        if (null == mode) {
            mode = FPEMode.FF1;
        }
        
        if (null == tweak) {
            switch (mode) {
                case FF1:
                    tweak = new byte[0];
                    break;
                case FF3_1:
                    // FF3-1要求必须为56 bits
                    tweak = new byte[7];
                    
            }
        }
        this.aes = new AES(mode.value, Padding.NoPadding.name(),
                KeyUtil.generateKey(mode.value, key),
                new FPEParameterSpec(mapper.getRadix(), tweak));
        
        this.sm4 = new SM4(mode.value, Padding.NoPadding.name(), KeyUtil.generateKey(mode.value, key), new FPEParameterSpec(mapper.getRadix(), tweak));
        this.mapper = mapper;
    }
    
    public FPEForSM42(FPEMode mode, byte[] key, AlphabetMapper mapper, byte[] tweak, String numberMapperStr) {
        if (null == mode) {
            mode = FPEMode.FF1;
        }
        
        if (null == tweak) {
            switch (mode) {
                case FF1:
                    tweak = new byte[0];
                    break;
                case FF3_1:
                    // FF3-1要求必须为56 bits
                    tweak = new byte[7];
            }
        }
        this.aes = new AES(mode.value, Padding.NoPadding.name(),
                KeyUtil.generateKey(mode.value, key),
                new FPEParameterSpec(mapper.getRadix(), tweak));
        
        this.sm4 = new SM4(mode.value, Padding.NoPadding.name(), KeyUtil.generateKey(mode.value, key), new FPEParameterSpec(mapper.getRadix(), tweak));
        this.mapper = mapper;
        String keyStr = new String(key);
        this.sortCode = calculateSourCode(keyStr);
        this.numberMapperStr = numberMapperStr;
        
    }
    
    // 字符长度较短时候不满足ff3加密规则时根据key做字符映射
    private int calculateSourCode(String key) {
        int i = 0;
        char[] chars = key.toCharArray();
        for (char aChar : chars) {
            if (Character.isDigit(aChar)) {
                i += aChar;
            } else {
                i += letter2num(aChar);
            }
        }
        return i;
    }
    
    private int letter2num(char character) {
        return character - 96 > 0 ? character - 96 : character - 64;
    }
    
    /**
     * 加密
     *
     * @param data 数据，数据必须在构造传入的{@link AlphabetMapper}中定义的范围
     * @return 密文结果
     */
    public String encrypt(String data) {
        if (null == data) {
            return null;
        }
        try {
            boolean isNumeric = "0123456789".equals(numberMapperStr);
            if (isNumeric && data.length() > 1) {
                // 首位用1-9做简单加密
                String firstDigitSet = numberMapperStr.substring(1); // "123456789"
                char firstChar = data.charAt(0);
                int iFirst = firstDigitSet.indexOf(firstChar);
                if (iFirst == -1)
                    iFirst = 0; // 容错
                int jFirst = (sortCode) % firstDigitSet.length();
                int index = (iFirst + jFirst) % firstDigitSet.length();
                char encFirst = firstDigitSet.charAt(index);
                // 剩余部分走原有逻辑
                String rest = data.substring(1);
                String encRest;
                if (rest.length() == 0) {
                    encRest = "";
                } else if (rest.length() < 2 || Math.pow(this.mapper.getRadix(), rest.getBytes(StandardCharsets.UTF_8).length) < 1000000) {
                    List<String> list = toList(rest);
                    StringBuilder encrypt = new StringBuilder();
                    for (int idx = 0; idx < list.size(); idx++) {
                        String s = list.get(idx);
                        int i = numberMapperStr.indexOf(s);
                        int j = (sortCode + idx + 1) % numberMapperStr.length(); // idx+1保证扰动不和首位重复
                        int idxEnc = (i + j) % numberMapperStr.length();
                        encrypt.append(numberMapperStr.charAt(idxEnc));
                    }
                    encRest = encrypt.toString();
                } else {
                    List<String> plainValues = checkArgs(this.mapper.getRadix(), rest);
                    StringBuilder encryptedText = new StringBuilder();
                    for (String plainValue : plainValues) {
                        encryptedText.append(new String(encrypt(plainValue.toCharArray())));
                    }
                    encRest = encryptedText.toString();
                }
                return encFirst + encRest;
            }
            // 非数字集或长度为1，走原有逻辑
            if (data.length() < 2 || Math.pow(this.mapper.getRadix(), data.getBytes(StandardCharsets.UTF_8).length) < 1000000) {
                List<String> list = toList(data);
                StringBuilder encrypt = new StringBuilder();
                for (int idx = 0; idx < list.size(); idx++) {
                    String s = list.get(idx);
                    int i = numberMapperStr.indexOf(s);
                    int j = (sortCode + idx) % numberMapperStr.length(); // 每位扰动
                    int index = (i + j) % numberMapperStr.length();
                    encrypt.append(numberMapperStr.charAt(index));
                }
                return encrypt.toString();
            }
            List<String> plainValues = checkArgs(this.mapper.getRadix(), data);
            StringBuilder encryptedText = new StringBuilder();
            for (String plainValue : plainValues) {
                encryptedText.append(new String(encrypt(plainValue.toCharArray())));
            }
            return encryptedText.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
    
    private List<String> toList(String data) {
        List<String> list = new ArrayList<String>();
        for (char c : data.toCharArray()) {
            list.add(String.valueOf(c));
        }
        return list;
    }
    
    /**
     * 加密
     *
     * @param data 数据，数据必须在构造传入的{@link AlphabetMapper}中定义的范围
     * @return 密文结果
     */
    public char[] encrypt(char[] data) {
        if (null == data) {
            return null;
        }
        // 通过 mapper 将密文输出处理为原始格式
        return mapper.convertToChars(sm4.encrypt(mapper.convertToIndexes(data)));
    }
    
    /**
     * 解密
     *
     * @param data 密文数据，数据必须在构造传入的{@link AlphabetMapper}中定义的范围
     * @return 明文结果
     */
    public String decrypt(String data) {
        if (null == data) {
            return null;
        }
        try {
            boolean isNumeric = "0123456789".equals(numberMapperStr);
            if (isNumeric && data.length() > 1) {
                // 首位用1-9做简单解密
                String firstDigitSet = numberMapperStr.substring(1); // "123456789"
                char firstChar = data.charAt(0);
                int iFirst = firstDigitSet.indexOf(firstChar);
                if (iFirst == -1)
                    iFirst = 0; // 容错
                int jFirst = (sortCode) % firstDigitSet.length();
                int index = iFirst - jFirst;
                if (index < 0) {
                    index = (firstDigitSet.length() + index) % firstDigitSet.length();
                }
                char decFirst = firstDigitSet.charAt(index);
                // 剩余部分走原有逻辑
                String rest = data.substring(1);
                String decRest;
                if (rest.length() == 0) {
                    decRest = "";
                } else if (rest.length() < 2 || Math.pow(this.mapper.getRadix(), rest.getBytes(StandardCharsets.UTF_8).length) < 1000000) {
                    List<String> list = toList(rest);
                    StringBuilder decrypt = new StringBuilder();
                    for (int idx = 0; idx < list.size(); idx++) {
                        String s = list.get(idx);
                        int j = (sortCode + idx + 1) % numberMapperStr.length();
                        int i = numberMapperStr.indexOf(s);
                        int idxDec = i - j;
                        if (idxDec < 0) {
                            idxDec = (numberMapperStr.length() + idxDec) % numberMapperStr.length();
                        }
                        decrypt.append(numberMapperStr.charAt(idxDec));
                    }
                    decRest = decrypt.toString();
                } else {
                    List<String> plainValues = checkArgs(this.mapper.getRadix(), rest);
                    StringBuilder decryptedText = new StringBuilder();
                    for (String plainValue : plainValues) {
                        decryptedText.append(new String(decrypt(plainValue.toCharArray())));
                    }
                    decRest = decryptedText.toString();
                }
                return decFirst + decRest;
            }
            // 非数字集或长度为1，走原有逻辑
            if (data.length() < 2 || Math.pow(this.mapper.getRadix(), data.getBytes(StandardCharsets.UTF_8).length) < 1000000) {
                List<String> list = toList(data);
                StringBuilder decrypt = new StringBuilder();
                for (int idx = 0; idx < list.size(); idx++) {
                    String s = list.get(idx);
                    int j = (sortCode + idx) % numberMapperStr.length();
                    int i = numberMapperStr.indexOf(s);
                    int index = i - j;
                    if (index < 0) {
                        index = (numberMapperStr.length() + index) % numberMapperStr.length();
                    }
                    decrypt.append(numberMapperStr.charAt(index));
                }
                return decrypt.toString();
            }
            List<String> plainValues = checkArgs(this.mapper.getRadix(), data);
            StringBuilder decryptedText = new StringBuilder();
            for (String plainValue : plainValues) {
                decryptedText.append(new String(decrypt(plainValue.toCharArray())));
            }
            return decryptedText.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
    
    /**
     * 加密
     *
     * @param data 密文数据，数据必须在构造传入的{@link AlphabetMapper}中定义的范围
     * @return 明文结果
     */
    public char[] decrypt(char[] data) {
        if (null == data) {
            return null;
        }
        // 通过 mapper 将密文输出处理为原始格式
        try {
            return mapper.convertToChars(sm4.decrypt(mapper.convertToIndexes(data)));
        } catch (Exception e) {
            // e.printStackTrace();
            return data;
        }
        
    }
    
    /**
     * 校验大小，超过ff3范围则拆分
     * @param radix
     * @param data
     * @return
     */
    private List<String> checkArgs(int radix, String data) {
        List<String> sourceList = new ArrayList<String>();
        int maxLen = 2 * (int) (Math.floor(Math.log(TWO_TO_96) / Math.log(radix)));
        if (data.getBytes(StandardCharsets.UTF_8).length > maxLen) {
            sourceList = splitString(data, maxLen);
        } else {
            sourceList.add(data);
        }
        return sourceList;
    }
    
    private List<String> splitString(String str, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = str.length();
        
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(str.substring(i, end));
        }
        
        return chunks;
    }
    
    /**
     * FPE模式<br>
     * FPE包括两种模式：FF1和FF3（FF2弃用），核心均为Feistel网络结构。
     */
    public enum FPEMode {
        
        /**
         * FF1模式
         */
        FF1("FF1"),
        /**
         * FF3-1 模式
         */
        FF3_1("FF3-1");
        
        private final String value;
        
        FPEMode(String name) {
            this.value = name;
        }
        
        /**
         * 获取模式名
         *
         * @return 模式名
         */
        public String getValue() {
            return value;
        }
    }
    
}