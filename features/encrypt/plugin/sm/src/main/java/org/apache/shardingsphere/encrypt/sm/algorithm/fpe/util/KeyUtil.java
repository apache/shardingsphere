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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * 密钥工具类
 *
 * <p>
 * 包括:
 * <pre>
 * 1、生成密钥（单密钥、密钥对）
 * 2、读取密钥文件
 * </pre>
 *
 * @author looly, Gsealy
 * @since 4.4.1
 */
public class KeyUtil {
    
    /**
     * Java密钥库(Java Key Store，JKS)KEY_STORE
     */
    public static final String KEY_TYPE_JKS = "JKS";
    /**
     * jceks
     */
    public static final String KEY_TYPE_JCEKS = "jceks";
    /**
     * PKCS12是公钥加密标准，它规定了可包含所有私钥、公钥和证书。其以二进制格式存储，也称为 PFX 文件
     */
    public static final String KEY_TYPE_PKCS12 = "pkcs12";
    /**
     * Certification类型：X.509
     */
    public static final String CERT_TYPE_X509 = "X.509";
    
    /**
     * 默认密钥字节数
     *
     * <pre>
     * RSA/DSA
     * Default Keysize 1024
     * Keysize must be a multiple of 64, ranging from 512 to 1024 (inclusive).
     * </pre>
     */
    public static final int DEFAULT_KEY_SIZE = 1024;
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法，支持PBE算法
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm) {
        return generateKey(algorithm, -1);
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成<br>
     * 当指定keySize&lt;0时，AES默认长度为128，其它算法不指定。
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度，&lt;0表示不设定密钥长度，即使用默认长度
     * @return {@link SecretKey}
     * @since 3.1.2
     */
    public static SecretKey generateKey(String algorithm, int keySize) {
        return generateKey(algorithm, keySize, null);
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成<br>
     * 当指定keySize&lt;0时，AES默认长度为128，其它算法不指定。
     *
     * @param algorithm 算法，支持PBE算法
     * @param keySize   密钥长度，&lt;0表示不设定密钥长度，即使用默认长度
     * @param random    随机数生成器，null表示默认
     * @return {@link SecretKey}
     * @since 5.5.2
     */
    public static SecretKey generateKey(String algorithm, int keySize, SecureRandom random) {
        algorithm = getMainAlgorithm(algorithm);
        
        final KeyGenerator keyGenerator = getKeyGenerator(algorithm);
        if (keySize <= 0 && SymmetricAlgorithm.AES.getValue().equals(algorithm)) {
            // 对于AES的密钥，除非指定，否则强制使用128位
            keySize = 128;
        }
        
        if (keySize > 0) {
            if (null == random) {
                keyGenerator.init(keySize);
            } else {
                keyGenerator.init(keySize, random);
            }
        }
        return keyGenerator.generateKey();
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法密钥生成
     *
     * @param algorithm 算法
     * @param key       密钥，如果为{@code null} 自动生成随机密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, byte[] key) {
        Assert.notBlank(algorithm, "Algorithm is blank!");
        SecretKey secretKey;
        if (algorithm.startsWith("PBE")) {
            // PBE密钥
            secretKey = generatePBEKey(algorithm, (null == key) ? null : StrUtil.utf8Str(key).toCharArray());
        } else if (algorithm.startsWith("DES")) {
            // DES密钥
            secretKey = generateDESKey(algorithm, key);
        } else {
            // 其它算法密钥
            secretKey = (null == key) ? generateKey(algorithm) : new SecretKeySpec(key, algorithm);
        }
        return secretKey;
    }
    
    /**
     * 生成 {@link SecretKey}
     *
     * @param algorithm DES算法，包括DES、DESede等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generateDESKey(String algorithm, byte[] key) {
        if (StrUtil.isBlank(algorithm) || false == algorithm.startsWith("DES")) {
            throw new CryptoException("Algorithm [{}] is not a DES algorithm!");
        }
        
        SecretKey secretKey;
        if (null == key) {
            secretKey = generateKey(algorithm);
        } else {
            KeySpec keySpec;
            try {
                if (algorithm.startsWith("DESede")) {
                    // DESede兼容
                    keySpec = new DESedeKeySpec(key);
                } else {
                    keySpec = new DESKeySpec(key);
                }
            } catch (InvalidKeyException e) {
                throw new CryptoException(e);
            }
            secretKey = generateKey(algorithm, keySpec);
        }
        return secretKey;
    }
    
    /**
     * 生成PBE {@link SecretKey}
     *
     * @param algorithm PBE算法，包括：PBEWithMD5AndDES、PBEWithSHA1AndDESede、PBEWithSHA1AndRC2_40等
     * @param key       密钥
     * @return {@link SecretKey}
     */
    public static SecretKey generatePBEKey(String algorithm, char[] key) {
        if (StrUtil.isBlank(algorithm) || false == algorithm.startsWith("PBE")) {
            throw new CryptoException("Algorithm [{}] is not a PBE algorithm!");
        }
        
        if (null == key) {
            key = RandomUtil.randomString(32).toCharArray();
        }
        PBEKeySpec keySpec = new PBEKeySpec(key);
        return generateKey(algorithm, keySpec);
    }
    
    /**
     * 生成 {@link SecretKey}，仅用于对称加密和摘要算法
     *
     * @param algorithm 算法
     * @param keySpec   {@link KeySpec}
     * @return {@link SecretKey}
     */
    public static SecretKey generateKey(String algorithm, KeySpec keySpec) {
        final SecretKeyFactory keyFactory = getSecretKeyFactory(algorithm);
        try {
            return keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * 获取{@link SecretKeyFactory}
     *
     * @param algorithm 对称加密算法
     * @return {@link KeyFactory}
     * @since 4.5.2
     */
    public static SecretKeyFactory getSecretKeyFactory(String algorithm) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        SecretKeyFactory keyFactory;
        try {
            keyFactory = (null == provider) //
                    ? SecretKeyFactory.getInstance(getMainAlgorithm(algorithm)) //
                    : SecretKeyFactory.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return keyFactory;
    }
    
    /**
     * 获取{@link KeyGenerator}
     *
     * @param algorithm 对称加密算法
     * @return {@link KeyGenerator}
     * @since 4.5.2
     */
    public static KeyGenerator getKeyGenerator(String algorithm) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        KeyGenerator generator;
        try {
            generator = (null == provider) //
                    ? KeyGenerator.getInstance(getMainAlgorithm(algorithm)) //
                    : KeyGenerator.getInstance(getMainAlgorithm(algorithm), provider);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        return generator;
    }
    
    /**
     * 获取主体算法名，例如RSA/ECB/PKCS1Padding的主体算法是RSA
     *
     * @param algorithm XXXwithXXX算法
     * @return 主体算法名
     * @since 4.5.2
     */
    public static String getMainAlgorithm(String algorithm) {
        Assert.notBlank(algorithm, "Algorithm must be not blank!");
        final int slashIndex = algorithm.indexOf(CharUtil.SLASH);
        if (slashIndex > 0) {
            return algorithm.substring(0, slashIndex);
        }
        return algorithm;
    }
    
    /**
     * 获取用于密钥生成的算法<br>
     * 获取XXXwithXXX算法的后半部分算法，如果为ECDSA或SM2，返回算法为EC
     *
     * @param algorithm XXXwithXXX算法
     * @return 算法
     */
    public static String getAlgorithmAfterWith(String algorithm) {
        Assert.notNull(algorithm, "algorithm must be not null !");
        
        if (StrUtil.startWithIgnoreCase(algorithm, "ECIESWith")) {
            return "EC";
        }
        
        int indexOfWith = StrUtil.lastIndexOfIgnoreCase(algorithm, "with");
        if (indexOfWith > 0) {
            algorithm = StrUtil.subSuf(algorithm, indexOfWith + "with".length());
        }
        if ("ECDSA".equalsIgnoreCase(algorithm)
                || "SM2".equalsIgnoreCase(algorithm)
                || "ECIES".equalsIgnoreCase(algorithm)) {
            algorithm = "EC";
        }
        return algorithm;
    }
    
    /**
     * 读取密钥库(Java Key Store，JKS) KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param keyFile  证书文件
     * @param password 密码
     * @return {@link KeyStore}
     * @since 5.0.0
     */
    public static KeyStore readJKSKeyStore(File keyFile, char[] password) {
        return readKeyStore(KEY_TYPE_JKS, keyFile, password);
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
        return readKeyStore(KEY_TYPE_JKS, in, password);
    }
    
    /**
     * 读取PKCS12 KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param keyFile  证书文件
     * @param password 密码
     * @return {@link KeyStore}
     * @since 5.0.0
     */
    public static KeyStore readPKCS12KeyStore(File keyFile, char[] password) {
        return readKeyStore(KEY_TYPE_PKCS12, keyFile, password);
    }
    
    /**
     * 读取PKCS12 KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存
     *
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码
     * @return {@link KeyStore}
     * @since 5.0.0
     */
    public static KeyStore readPKCS12KeyStore(InputStream in, char[] password) {
        return readKeyStore(KEY_TYPE_PKCS12, in, password);
    }
    
    /**
     * 读取KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型
     * @param keyFile  证书文件
     * @param password 密码，null表示无密码
     * @return {@link KeyStore}
     * @since 5.0.0
     */
    public static KeyStore readKeyStore(String type, File keyFile, char[] password) {
        InputStream in = null;
        try {
            in = FileUtil.getInputStream(keyFile);
            return readKeyStore(type, in, password);
        } finally {
            IoUtil.close(in);
        }
    }
    
    /**
     * 读取KeyStore文件<br>
     * KeyStore文件用于数字证书的密钥对保存<br>
     * see: http://snowolf.iteye.com/blog/391931
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码，null表示无密码
     * @return {@link KeyStore}
     */
    public static KeyStore readKeyStore(String type, InputStream in, char[] password) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(type);
            keyStore.load(in, password);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
        return keyStore;
    }
    
    /**
     * 从KeyStore中获取私钥公钥
     *
     * @param type     类型
     * @param in       {@link InputStream} 如果想从文件读取.keystore文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyPair}
     * @since 4.4.1
     */
    public static KeyPair getKeyPair(String type, InputStream in, char[] password, String alias) {
        final KeyStore keyStore = readKeyStore(type, in, password);
        return getKeyPair(keyStore, password, alias);
    }
    
    /**
     * 从KeyStore中获取私钥公钥
     *
     * @param keyStore {@link KeyStore}
     * @param password 密码
     * @param alias    别名
     * @return {@link KeyPair}
     * @since 4.4.1
     */
    public static KeyPair getKeyPair(KeyStore keyStore, char[] password, String alias) {
        PublicKey publicKey;
        PrivateKey privateKey;
        try {
            publicKey = keyStore.getCertificate(alias).getPublicKey();
            privateKey = (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
        return new KeyPair(publicKey, privateKey);
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
        return readCertificate(CERT_TYPE_X509, in, password, alias);
    }
    
    /**
     * 读取X.509 Certification文件中的公钥<br>
     * Certification为证书文件<br>
     * see: https://www.cnblogs.com/yinliang/p/10115519.html
     *
     * @param in {@link InputStream} 如果想从文件读取.cer文件，使用 {@link FileUtil#getInputStream(File)} 读取
     * @return {@link KeyStore}
     * @since 4.5.2
     */
    public static PublicKey readPublicKeyFromCert(InputStream in) {
        final Certificate certificate = readX509Certificate(in);
        if (null != certificate) {
            return certificate.getPublicKey();
        }
        return null;
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
        return readCertificate(CERT_TYPE_X509, in);
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
        final KeyStore keyStore = readKeyStore(type, in, password);
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new CryptoException(e);
        }
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
        try {
            return getCertificateFactory(type).generateCertificate(in);
        } catch (CertificateException e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * 获得 Certification
     *
     * @param keyStore {@link KeyStore}
     * @param alias    别名
     * @return {@link Certificate}
     */
    public static Certificate getCertificate(KeyStore keyStore, String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
    
    /**
     * 获取{@link CertificateFactory}
     *
     * @param type 类型，例如X.509
     * @return {@link KeyPairGenerator}
     * @since 4.5.0
     */
    public static CertificateFactory getCertificateFactory(String type) {
        final Provider provider = GlobalBouncyCastleProvider.INSTANCE.getProvider();
        
        CertificateFactory factory;
        try {
            factory = (null == provider) ? CertificateFactory.getInstance(type) : CertificateFactory.getInstance(type, provider);
        } catch (CertificateException e) {
            throw new CryptoException(e);
        }
        return factory;
    }
    
    /**
     * 编码压缩EC公钥（基于BouncyCastle）<br>
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @return 压缩得到的X
     * @since 4.4.4
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey) {
        return BCUtil.encodeECPublicKey(publicKey);
    }
    
}
