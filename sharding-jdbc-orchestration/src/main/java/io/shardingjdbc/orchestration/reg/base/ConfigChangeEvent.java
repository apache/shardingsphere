package io.shardingjdbc.orchestration.reg.base;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * Config change event
 *
 * @author junxiong
 */
@Value
@Wither
@AllArgsConstructor(staticName = "with")
public class ConfigChangeEvent<C> {
    C newConfig;
}
