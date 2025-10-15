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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.fpe.FPE;
import org.bouncycastle.crypto.AlphabetMapper;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.KeySpec;
import java.util.Map;

/**
 * 安全相关工具类<br>
 * 加密分为三种：<br>
 * 1、对称加密（symmetric），例如：AES、DES等<br>
 * 2、非对称加密（asymmetric），例如：RSA、DSA等<br>
 * 3、摘要加密（digest），例如：MD5、SHA-1、SHA-256、HMAC等<br>
 *
 * @author Looly, Gsealy
 */
public class SecureUtil {
    
    /**
     * 默认密钥字节数
     *
     * <pre>
     * RSA/DSA
     * Default Keysize 1024
     * Keysize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * </pre>
     */
    public static final int DEFAULT_KEY_SIZE = KeyUtil.DEFAULT_KEY_SIZE;
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法，支持PBE算法
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm) {
        return KeyUtil.generateKey(algorithm);
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度
     * @return {@link SecretKey}
     * @since 3.1.2
     */
    public static SecretKey generateKey(String algorithm, int keySize) {
        return KeyUtil.generateKey(algorithm, keySize);
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法
     * @param key       密钥，如果为{@code null} 自动生成随机密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, byte[] key) {
        return KeyUtil.generateKey(algorithm, key);
    }
    
    /**
     * 生成 {@link SecretKey}
     *
     * @param algorithm DES算法，包括DES、DESede等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateDESKey(String algorithm, byte[] key) {
        return KeyUtil.generateDESKey(algorithm, key);
    }
    
    /**
     * 生成PBE {@link SecretKey}
     *
     * @param algorithm PBE算法，包括：PBEWithMD5AndDES、PBEWithSHA1AndDESede、PBEWithSHA1AndRC2_40等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generatePBEKey(String algorithm, char[] key) {
        return KeyUtil.generatePBEKey(algorithm, key);
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, KeySpec keySpec) {
        return KeyUtil.generateKey(algorithm, keySpec);
    }
    
    /**
     * 获取用于密钥生成的算法<br>
     * 获取XXXwithXXX算法的后半部分算法，如果为ECDSA或SM2，返回算法为EC
     *
     * @param algorithm XXXwithXXX算法
     * @return 算法
     */
    public static String getAlgorithmAfterWith(String algorithm) {
        return KeyUtil.getAlgorithmAfterWith(algorithm);
    }
    
    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readJKSKeyStore(InputStream in, char[] password) {
        return KeyUtil.readJKSKeyStore(in, password);
    }
    
    /**
     * 读取KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(String type, InputStream in, char[] password) {
        return KeyUtil.readKeyStore(type, in, password);
    }
    
    /**
     * 读取X.509 Certification文件<br>
     * Certification为证书文件<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in       {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     * @since 4.4.1
     */
    public static Certificate readX509Certificate(InputStream in, char[] password, String alias) {
        return KeyUtil.readX509Certificate(in, password, alias);
    }
    
    /**
     * 读取X.509 Certification文件<br>
     * Certification为证书文件<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @return {@link KeyStore}
     * @since 4.4.1
     */
    public static Certificate readX509Certificate(InputStream in) {
        return KeyUtil.readX509Certificate(in);
    }
    
    /**
     * 读取Certification文件<br>
     * Certification为证书文件<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型，例如X.509
     * @param in       {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyStore}
     * @since 4.4.1
     */
    public static Certificate readCertificate(String type, InputStream in, char[] password, String alias) {
        return KeyUtil.readCertificate(type, in, password, alias);
    }
    
    /**
     * 读取Certification文件<br>
     * Certification为证书文件<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type 类型，例如X.509
     * @param in   {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @return {@link Certificate}
     */
    public static Certificate readCertificate(String type, InputStream in) {
        return KeyUtil.readCertificate(type, in);
    }
    
    /**
     * 获得 Certification
     *
     * @param keyStore {@link KeyStore}
     * @param alias    别名
     * @return {@link Certificate}
     */
    public static Certificate getCertificate(KeyStore keyStore, String alias) {
        return KeyUtil.getCertificate(keyStore, alias);
    }
    
    // ------------------------------------------------------------------- 对称加密算法
    
    /**
     * AES加密，生成随机KEY。注意解密时必须使用相同 {@link AES}对象或者使用相同KEY<br>
     * 例：
     *
     * <pre>
     * AES加密：aes().encrypt(data)
     * AES解密：aes().decrypt(data)
     * </pre>
     *
     * @return {@link AES}
     */
    public static AES aes() {
        return new AES();
    }
    
    /**
     * AES加密<br>
     * 例：
     *
     * <pre>
     * AES加密：aes(key).encrypt(data)
     * AES解密：aes(key).decrypt(data)
     * </pre>
     *
     * @param key 密钥
     * @return {@link SymmetricCrypto}
     */
    public static AES aes(byte[] key) {
        return new AES(key);
    }
    
    /**
     * 对参数做签名<br>
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串<br>
     * 拼接后的字符串键值对之间无符号，键值对之间无符号，忽略null值
     *
     * @param crypto      对称加密算法
     * @param params      参数
     * @param otherParams 其它附加参数字符串（例如密钥）
     * @return 签名
     * @since 4.0.1
     */
    public static String signParams(SymmetricCrypto crypto, Map<?, ?> params, String... otherParams) {
        return signParams(crypto, params, StrUtil.EMPTY, StrUtil.EMPTY, true, otherParams);
    }
    
    /**
     * 对参数做签名<br>
     * 参数签名为对Map参数按照key的顺序排序后拼接为字符串，然后根据提供的签名算法生成签名字符串
     *
     * @param crypto            对称加密算法
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 签名
     * @since 4.0.1
     */
    public static String signParams(SymmetricCrypto crypto, Map<?, ?> params, String separator,
                                    String keyValueSeparator, boolean isIgnoreNull, String... otherParams) {
        return crypto.encryptHex(MapUtil.sortJoin(params, separator, keyValueSeparator, isIgnoreNull, otherParams));
    }
    
    /**
     * 增加加密解密的算法提供者，默认优先使用，例如：
     *
     * <pre>
     * addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
     * </pre>
     *
     * @param provider 算法提供者
     * @since 4.1.22
     */
    public static void addProvider(Provider provider) {
        Security.insertProviderAt(provider, 0);
    }
    
    /**
     * 解码字符串密钥，可支持的编码如下：
     *
     * <pre>
     * 1. Hex（16进制）编码
     * 1. Base64编码
     * </pre>
     *
     * @param key 被解码的密钥字符串
     * @return 密钥
     * @since 4.3.3
     */
    public static byte[] decode(String key) {
        return Validator.isHex(key) ? HexUtil.decodeHex(key) : Base64.decode(key);
    }
    
    /**
     * 创建{@link Cipher}
     *
     * @param algorithm 算法
     * @return {@link Cipher}
     * @since 4.5.2
     */
    public static Cipher createCipher(String algorithm) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        Cipher cipher;
        try {
            cipher = (null == provider) ? Cipher.getInstance(algorithm) : Cipher.getInstance(algorithm, provider);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
        
        return cipher;
    }
    
    /**
     * 创建{@link MessageDigest}
     *
     * @param algorithm 算法
     * @return {@link MessageDigest}
     * @since 4.5.2
     */
    public static MessageDigest createMessageDigest(String algorithm) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        MessageDigest messageDigest;
        try {
            messageDigest = (null == provider) ? MessageDigest.getInstance(algorithm) : MessageDigest.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        
        return messageDigest;
    }
    
    /**
     * 创建{@link Mac}
     *
     * @param algorithm 算法
     * @return {@link Mac}
     * @since 4.5.13
     */
    public static Mac createMac(String algorithm) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        Mac mac;
        try {
            mac = (null == provider) ? Mac.getInstance(algorithm) : Mac.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        
        return mac;
    }
    
    /**
     * 创建{@link Signature}
     *
     * @param algorithm 算法
     * @return {@link Signature}
     * @since 5.7.0
     */
    public static Signature createSignature(String algorithm) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        Signature signature;
        try {
            signature = (null == provider) ? Signature.getInstance(algorithm) : Signature.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        
        return signature;
    }
    
    /**
     * FPE(Format Preserving Encryption)实现，支持FF1和FF3-1模式。
     *
     * @param mode   FPE模式枚举，可选FF1或FF3-1
     * @param key    密钥，{@code null}表示随机密钥，长度必须是16bit、24bit或32bit
     * @param mapper Alphabet字典映射，被加密的字符范围和这个映射必须一致，例如手机号、银行卡号等字段可以采用数字字母字典表
     * @param tweak  Tweak是为了解决因局部加密而导致结果冲突问题，通常情况下将数据的不可变部分作为Tweak
     * @return {@link FPE}
     * @since 5.7.12
     */
    public static FPE fpe(FPE.FPEMode mode, byte[] key, AlphabetMapper mapper, byte[] tweak) {
        return new FPE(mode, key, mapper, tweak);
    }
}
