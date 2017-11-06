package io.shardingjdbc.orchestration.reg.base;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * State Change Event
 *
 * @author junxiong
 */
@Value
@Wither
@AllArgsConstructor(staticName = "with")
public class StateChangeEvent {
    ShardState newState;
}
