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

package org.apache.shardingsphere.db.protocol.opengauss.packet.authentication;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.opengauss.constant.OpenGaussProtocolVersion;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;

/**
 * Authentication request SCRAM SHA-256 for openGauss.
 */
public final class OpenGaussAuthenticationSCRAMSha256Packet implements PostgreSQLIdentifierPacket {
    
    private static final String PBKDF2_WITH_HMAC_SHA1_ALGORITHM = "PBKDF2WithHmacSHA1";
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    private static final String SERVER_KEY = "Server Key";
    
    private static final int AUTH_REQ_SHA256 = 10;
    
    private static final int PASSWORD_STORED_METHOD_SHA256 = 2;
    
    private final byte[] random64Code;
    
    private final byte[] token;
    
    private final int version;
    
    private final byte[] serverSignature;
    
    private final int serverIteration;
    
    public OpenGaussAuthenticationSCRAMSha256Packet(final String password, final OpenGaussAuthenticationHexData authHexData, final int version, final int serverIteration) {
        random64Code = authHexData.getSalt().getBytes();
        token = authHexData.getNonce().getBytes();
        this.version = version;
        serverSignature = (version >= OpenGaussProtocolVersion.PROTOCOL_350.getVersion() ? "" : calculateServerSignature(password, authHexData, serverIteration)).getBytes();
        this.serverIteration = serverIteration;
    }
    
    private static String calculateServerSignature(final String password, final OpenGaussAuthenticationHexData authHexData, final int serverIteration) {
        byte[] k = generateKFromPBKDF2(password, authHexData.getSalt(), serverIteration);
        byte[] serverKey = getKeyFromHmac(k, SERVER_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] result = getKeyFromHmac(serverKey, hexStringToBytes(authHexData.getNonce()));
        return bytesToHexString(result);
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    private static byte[] generateKFromPBKDF2(final String password, final String saltString, final int serverIteration) {
        char[] chars = password.toCharArray();
        byte[] salt = hexStringToBytes(saltString);
        PBEKeySpec spec = new PBEKeySpec(chars, salt, serverIteration, 32 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA1_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
    
    private static byte[] hexStringToBytes(final String rawHexString) {
        if (null == rawHexString || rawHexString.isEmpty()) {
            return new byte[0];
        }
        String hexString = rawHexString.toUpperCase(Locale.ENGLISH);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            result[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return result;
    }
    
    private static byte charToByte(final char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeyException.class})
    private static byte[] getKeyFromHmac(final byte[] key, final byte[] data) {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        return mac.doFinal(data);
    }
    
    private static String bytesToHexString(final byte[] src) {
        StringBuilder result = new StringBuilder();
        for (byte each : src) {
            String hex = Integer.toHexString(each & 255);
            if (hex.length() < 2) {
                result.append(0);
            }
            result.append(hex);
        }
        return result.toString();
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt4(AUTH_REQ_SHA256);
        payload.writeInt4(PASSWORD_STORED_METHOD_SHA256);
        payload.writeBytes(random64Code);
        payload.writeBytes(token);
        if (version < OpenGaussProtocolVersion.PROTOCOL_350.getVersion()) {
            payload.writeBytes(serverSignature);
        }
        if (OpenGaussProtocolVersion.PROTOCOL_351.getVersion() == version) {
            payload.writeInt4(serverIteration);
        }
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.AUTHENTICATION_REQUEST;
    }
}
