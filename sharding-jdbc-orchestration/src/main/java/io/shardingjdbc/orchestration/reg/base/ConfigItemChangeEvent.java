package io.shardingjdbc.orchestration.reg.base;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

/**
 * Config item change listener
 *
 * @author junxiong
 */
@Value
@Wither
@AllArgsConstructor(staticName = "with")
public class ConfigItemChangeEvent<I> {
    List<I> newItems;
}
