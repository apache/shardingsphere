/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.handshake;

import com.google.common.base.Strings;
import io.shardingsphere.core.rule.ProxyAuthority;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;

/**
 * Check authority of user.
 *
 * @author panjuan
 */
@Getter
public class AuthorityHandler {
    
    private final AuthPluginData authPluginData;
    
    public AuthorityHandler() {
        authPluginData = new AuthPluginData();
    }
    
    /**
     * Login into sharding proxy.
     *
     * @param username connection username.
     * @param authResponse connection auth response.
     * @return login success or failure.
     */
    public boolean login(final String username, final byte[] authResponse) {
        ProxyAuthority proxyAuthority = RuleRegistry.getInstance().getProxyAuthority();
        if (Strings.isNullOrEmpty(proxyAuthority.getPassword())) {
            return proxyAuthority.getUsername().equals(username);
        }
        return proxyAuthority.getUsername().equals(username) && Arrays.equals(getAuthCipherBytes(proxyAuthority.getPassword()), authResponse);
    }
    
    private byte[] getAuthCipherBytes(final String password) {
        byte[] sha1Password = DigestUtils.sha1(password);
        byte[] doubleSha1Password = DigestUtils.sha1(sha1Password);
        byte[] concatBytes = new byte[authPluginData.getAuthPluginData().length + doubleSha1Password.length];
        System.arraycopy(authPluginData.getAuthPluginData(), 0, concatBytes, 0, authPluginData.getAuthPluginData().length);
        System.arraycopy(doubleSha1Password, 0, concatBytes, authPluginData.getAuthPluginData().length, doubleSha1Password.length);
        byte[] sha1ConcatBytes = DigestUtils.sha1(concatBytes);
        return xor(sha1Password, sha1ConcatBytes);
    }
    
    private byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            result[i] = (byte) (input[i] ^ secret[i]);
        }
        return result;
    }
}
