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

import cn.hutool.core.io.IORuntimeException;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Bouncy Castle相关工具类封装
 *
 * @author looly
 * @since 4.5.0
 */
public class BCUtil {
    
    /**
     * 只获取私钥里的d，32字节
     *
     * @param privateKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
     * @return 压缩得到的X
     * @since 5.1.6
     */
    public static byte[] encodeECPrivateKey(PrivateKey privateKey) {
        return ((BCECPrivateKey) privateKey).getD().toByteArray();
    }
    
    /**
     * 编码压缩EC公钥（基于BouncyCastle），即Q值<br>
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @return 压缩得到的Q
     * @since 4.4.4
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey) {
        return encodeECPublicKey(publicKey, true);
    }
    
    /**
     * 编码压缩EC公钥（基于BouncyCastle），即Q值<br>
     * 见：https://www.cnblogs.com/xinzhao/p/8963724.html
     *
     * @param publicKey    {@link PublicKey}，必须为org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
     * @param isCompressed 是否压缩
     * @return 得到的Q
     * @since 5.5.9
     */
    public static byte[] encodeECPublicKey(PublicKey publicKey, boolean isCompressed) {
        return ((BCECPublicKey) publicKey).getQ().getEncoded(isCompressed);
    }
    
    /**
     * 构建ECDomainParameters对象
     *
     * @param parameterSpec ECParameterSpec
     * @return {@link ECDomainParameters}
     * @since 5.2.0
     */
    public static ECDomainParameters toDomainParams(ECParameterSpec parameterSpec) {
        return new ECDomainParameters(
                parameterSpec.getCurve(),
                parameterSpec.getG(),
                parameterSpec.getN(),
                parameterSpec.getH());
    }
    
    /**
     * 构建ECDomainParameters对象
     *
     * @param curveName Curve名称
     * @return {@link ECDomainParameters}
     * @since 5.2.0
     */
    public static ECDomainParameters toDomainParams(String curveName) {
        return toDomainParams(ECUtil.getNamedCurveByName(curveName));
    }
    
    /**
     * 构建ECDomainParameters对象
     *
     * @param x9ECParameters {@link X9ECParameters}
     * @return {@link ECDomainParameters}
     * @since 5.2.0
     */
    public static ECDomainParameters toDomainParams(X9ECParameters x9ECParameters) {
        return new ECDomainParameters(
                x9ECParameters.getCurve(),
                x9ECParameters.getG(),
                x9ECParameters.getN(),
                x9ECParameters.getH());
    }
    
    /**
     * Java中的X.509格式公钥转换为OpenSSL支持的PKCS#1格式
     *
     * @param publicKey X.509格式公钥
     * @return PKCS#1格式公钥
     * @since 5.5.9
     */
    public static byte[] toPkcs1(PublicKey publicKey) {
        final SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo
                .getInstance(publicKey.getEncoded());
        try {
            return spkInfo.parsePublicKey().getEncoded();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
