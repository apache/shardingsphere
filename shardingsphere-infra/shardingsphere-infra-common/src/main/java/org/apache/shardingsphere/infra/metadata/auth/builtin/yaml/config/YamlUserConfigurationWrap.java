package org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.yaml.config.YamlConfiguration;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rule configuration warp for YAML.
 */
@Getter
@Setter
public final class YamlUserConfigurationWrap implements YamlConfiguration {
    private Collection<YamlUserConfiguration> users = new LinkedList<>();
}
