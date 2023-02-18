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
    
    private final int version;
    
    private final int serverIteration;
    
    private final OpenGaussAuthenticationHexData authHexData;
    
    private final String serverSignature;
    
    public OpenGaussAuthenticationSCRAMSha256Packet(final int version, final int serverIteration, final OpenGaussAuthenticationHexData authHexData, final String password) {
        this.version = version;
        this.serverIteration = serverIteration;
        this.authHexData = authHexData;
        serverSignature = version >= OpenGaussProtocolVersion.PROTOCOL_350.getVersion() ? "" : generateServerSignature(password);
    }
    
    private String generateServerSignature(final String password) {
        byte[] k = generateKFromPBKDF2(password);
        byte[] serverKey = getKeyFromHmac(k, SERVER_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] result = getKeyFromHmac(serverKey, toHexBytes(authHexData.getNonce()));
        return bytesToHexString(result);
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    private byte[] generateKFromPBKDF2(final String password) {
        char[] chars = password.toCharArray();
        byte[] salt = toHexBytes(authHexData.getSalt());
        PBEKeySpec spec = new PBEKeySpec(chars, salt, serverIteration, 32 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_WITH_HMAC_SHA1_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
    
    private byte[] toHexBytes(final String hexString) {
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toUpperCase(Locale.ENGLISH).toCharArray();
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            result[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return result;
    }
    
    private byte charToByte(final char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, InvalidKeyException.class})
    private byte[] getKeyFromHmac(final byte[] key, final byte[] data) {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        return mac.doFinal(data);
    }
    
    private String bytesToHexString(final byte[] src) {
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
        payload.writeBytes(authHexData.getSalt().getBytes());
        payload.writeBytes(authHexData.getNonce().getBytes());
        if (version < OpenGaussProtocolVersion.PROTOCOL_350.getVersion()) {
            payload.writeBytes(serverSignature.getBytes());
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
