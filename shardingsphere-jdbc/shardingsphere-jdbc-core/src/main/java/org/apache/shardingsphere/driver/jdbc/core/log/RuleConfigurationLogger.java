package org.apache.shardingsphere.driver.jdbc.core.log;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rule configuration logger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class RuleConfigurationLogger {
    
    /**
     * Log encrypt rule configuration.
     *
     * @param ruleConfiguration rule configuration
     */
    public static void log(final RuleConfiguration ruleConfiguration) {
        if (null == ruleConfiguration) {
            return;
        }
        if (ruleConfiguration instanceof EncryptRuleConfiguration) {
            log((EncryptRuleConfiguration) ruleConfiguration);
        }
    }
    
    private static void log(final EncryptRuleConfiguration encryptRuleConfiguration) {
        if (null != encryptRuleConfiguration) {
            log(encryptRuleConfiguration.getClass().getSimpleName(), YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swapToYamlConfiguration(encryptRuleConfiguration)));
        }
    }
    
    private static void log(final String type, final String logContent) {
        log.info("{}:\n{}", type, logContent);
    }
}