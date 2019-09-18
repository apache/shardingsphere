package info.avalon566.shardingscaling.job.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
@AllArgsConstructor
public class Event {

    private EventType eventType;
}
