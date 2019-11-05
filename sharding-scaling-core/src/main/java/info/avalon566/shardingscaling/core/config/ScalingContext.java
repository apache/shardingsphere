package info.avalon566.shardingscaling.core.config;

import lombok.Getter;

@Getter
public class ScalingContext {

    private static final ScalingContext INSTANCE = new ScalingContext();

    private RuleConfiguration ruleConfiguration;

    private ServerConfiguration serverConfiguration;

    public static ScalingContext getInstance() {
        return INSTANCE;
    }

    public void init(RuleConfiguration ruleConfiguration, ServerConfiguration serverConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
        this.serverConfiguration = serverConfiguration;
    }

}
