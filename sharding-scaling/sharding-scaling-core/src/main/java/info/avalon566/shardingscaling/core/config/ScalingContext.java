package info.avalon566.shardingscaling.core.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ScalingContext {

    private static final ScalingContext INSTANCE = new ScalingContext();

    private RuleConfiguration ruleConfiguration;

    private ServerConfiguration serverConfiguration;

    /**
     * Get instance of Sharding-Scaling's context.
     *
     * @return instance of Sharding-Scaling's context.
     */
    public static ScalingContext getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize  Scaling context.
     *
     * @param ruleConfiguration ruleConfiguration
     * @param serverConfiguration serverConfiguration
     */
    public void init(final RuleConfiguration ruleConfiguration, final ServerConfiguration serverConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
        this.serverConfiguration = serverConfiguration;
    }

}
