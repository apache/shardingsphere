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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.AES;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.KeyUtil;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.Padding;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.SM4;
import org.bouncycastle.crypto.AlphabetMapper;
import org.bouncycastle.jcajce.spec.FPEParameterSpec;

public class FPEForSM4 {
    
    private final AES aes;
    private final SM4 sm4;
    private final AlphabetMapper mapper;
    private static final double TWO_TO_96 = Math.pow((double) 2.0F, (double) 96.0F);
    private int sortCode;
    private String numberMapperStr;
    
    public FPEForSM4(FPEMode mode, byte[] key, AlphabetMapper mapper) {
        this(mode, key, mapper, (byte[]) null);
    }
    
    public FPEForSM4(FPEMode mode, byte[] key, AlphabetMapper mapper, byte[] tweak) {
        this.sortCode = 0;
        if (null == mode) {
            mode = FPEForSM4.FPEMode.FF1;
        }
        
        if (null == tweak) {
            switch (mode) {
                case FF1:
                    tweak = new byte[0];
                    break;
                case FF3_1:
                    tweak = new byte[7];
            }
        }
        
        this.aes = new AES(mode.value, Padding.NoPadding.name(), KeyUtil.generateKey(mode.value, key), new FPEParameterSpec(mapper.getRadix(), tweak));
        this.sm4 = new SM4(mode.value, Padding.NoPadding.name(), KeyUtil.generateKey(mode.value, key), new FPEParameterSpec(mapper.getRadix(), tweak));
        this.mapper = mapper;
    }
    
    public FPEForSM4(FPEMode mode, byte[] key, AlphabetMapper mapper, byte[] tweak, String numberMapperStr) {
        this.sortCode = 0;
        if (null == mode) {
            mode = FPEForSM4.FPEMode.FF1;
        }
        
        if (null == tweak) {
            switch (mode) {
                case FF1:
                    tweak = new byte[0];
                    break;
                case FF3_1:
                    tweak = new byte[7];
            }
        }
        
        this.aes = new AES(mode.value, Padding.NoPadding.name(), KeyUtil.generateKey(mode.value, key), new FPEParameterSpec(mapper.getRadix(), tweak));
        this.sm4 = new SM4(mode.value, Padding.NoPadding.name(), KeyUtil.generateKey(mode.value, key), new FPEParameterSpec(mapper.getRadix(), tweak));
        this.mapper = mapper;
        String keyStr = new String(key);
        this.sortCode = this.calculateSourCode(keyStr);
        this.numberMapperStr = numberMapperStr;
    }
    
    private int calculateSourCode(String key) {
        int i = 0;
        char[] chars = key.toCharArray();
        
        for (char aChar : chars) {
            if (Character.isDigit(aChar)) {
                i += aChar;
            } else {
                i += this.letter2num(aChar);
            }
        }
        
        return i;
    }
    
    private int letter2num(char character) {
        return character - 96 > 0 ? character - 96 : character - 64;
    }
    
    public String encrypt(String data) {
        if (null == data) {
            return null;
        } else {
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
    }
    
    private List<String> toList(String data) {
        List<String> list = new ArrayList();
        
        for (char c : data.toCharArray()) {
            list.add(String.valueOf(c));
        }
        
        return list;
    }
    
    public char[] encrypt(char[] data) {
        return null == data ? null : this.mapper.convertToChars(this.sm4.encrypt(this.mapper.convertToIndexes(data)));
    }
    
    public String decrypt(String data) {
        if (null == data) {
            return null;
        } else {
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
    }
    
    public char[] decrypt(char[] data) {
        if (null == data) {
            return null;
        } else {
            try {
                return this.mapper.convertToChars(this.sm4.decrypt(this.mapper.convertToIndexes(data)));
            } catch (Exception var3) {
                return data;
            }
        }
    }
    
    private List<String> checkArgs(int radix, String data) {
        List<String> sourceList = new ArrayList();
        int maxLen = 2 * (int) Math.floor(Math.log(TWO_TO_96) / Math.log((double) radix));
        if (data.getBytes(StandardCharsets.UTF_8).length > maxLen) {
            sourceList = this.splitString(data, maxLen);
        } else {
            sourceList.add(data);
        }
        
        return sourceList;
    }
    
    private List<String> splitString(String str, int chunkSize) {
        List<String> chunks = new ArrayList();
        int length = str.length();
        
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(str.substring(i, end));
        }
        
        return chunks;
    }
    
    public static enum FPEMode {
        
        FF1("FF1"),
        FF3_1("FF3-1");
        
        private final String value;
        
        private FPEMode(String name) {
            this.value = name;
        }
        
        public String getValue() {
            return this.value;
        }
    }
}
