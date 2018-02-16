package io.shardingjdbc.server.packet.handshake;

import com.google.common.primitives.Bytes;
import lombok.Getter;

/**
 * Auth plugin data.
 * 
 * <p>
 *     The auth-plugin-data is the concatenation of strings auth-plugin-data-part-1 and auth-plugin-data-part-2.
 *     The auth-plugin-data-part-1's length is 8; The auth-plugin-data-part-2's length is 12.
 * </p>
 *
 * @author zhangliang
 */
@Getter
public final class AuthPluginData {
    
    private final byte[] authPluginDataPart1;
    
    private final byte[] authPluginDataPart2;
    
    private final byte[] authPluginData;
    
    public AuthPluginData() {
        authPluginDataPart1 = RandomGenerator.getInstance().generateRandomBytes(8);
        authPluginDataPart2 = RandomGenerator.getInstance().generateRandomBytes(12);
        authPluginData = Bytes.concat(authPluginDataPart1, authPluginDataPart2);
    }
}
