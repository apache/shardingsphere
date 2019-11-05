package info.avalon566.shardingscaling.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ScalingConfiguration {

    private RuleConfiguration ruleConfiguration;

    private ServerConfiguration serverConfiguration;
}
