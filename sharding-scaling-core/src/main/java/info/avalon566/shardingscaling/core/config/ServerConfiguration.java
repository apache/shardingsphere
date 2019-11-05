package info.avalon566.shardingscaling.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServerConfiguration {

    private Integer blockQueueSize;
    private Integer pushTimeout;
    private Integer concurrency;

}
