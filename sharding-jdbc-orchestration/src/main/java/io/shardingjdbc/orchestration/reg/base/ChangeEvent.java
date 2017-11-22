package io.shardingjdbc.orchestration.reg.base;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * Change data
 *
 * @author  junxiong
 */
@Value
public class ChangeEvent {
    private ChangeType changeType;
    private Optional<ChangeData> changeData;

    public ChangeEvent(@NonNull final ChangeType changeType, final ChangeData changeData) {
        this.changeType = changeType;
        this.changeData = Optional.fromNullable(changeData);
    }

    /**
     * change type
     */
    public enum ChangeType {
        UPDATED, DELETED, UNKNOWN
    }

    /**
     * change data
     */
    @Value @Wither @AllArgsConstructor
    public static class ChangeData {
        String key, value;
    }
}
