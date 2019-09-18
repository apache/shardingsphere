package info.avalon566.shardingscaling.job.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author avalon566
 */
@Data
public class RuleConfiguration {

    @Data
    public static final class YamlDataSourceParameter {
        private String url;
        private String username;
        private String password;
    }

    private Map<String, YamlDataSourceParameter> dataSources = new HashMap<>();
}
