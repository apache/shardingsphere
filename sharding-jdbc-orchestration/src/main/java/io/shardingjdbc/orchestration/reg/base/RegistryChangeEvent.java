package io.shardingjdbc.orchestration.reg.base;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * @author junxiong
 */
@Value
@Wither
@AllArgsConstructor
public class RegistryChangeEvent {
    private RegistryChangeType type;
    private Optional<Payload> payload;

    @Value
    @Wither
    @AllArgsConstructor
    public static class Payload {
        String key;
        String value;
    }
}
