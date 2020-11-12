package org.apache.shardingsphere.scaling.core.service;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;

/**
 * {@code RegistryRepository} holder.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistryRepositoryHolder {
    
    /**
     * {@code RegistryRepository} instance.
     */
    public static final RegistryRepository REGISTRY_REPOSITORY = getInstance();
    
    @Getter
    private static boolean available;
    
    private static RegistryRepository getInstance() {
        RegistryRepository result = null;
        YamlGovernanceConfiguration resumeBreakPoint = ScalingContext.getInstance().getServerConfig().getResumeBreakPoint();
        if (resumeBreakPoint != null) {
            result = createRegistryRepository(new GovernanceConfigurationYamlSwapper().swapToObject(resumeBreakPoint));
        }
        if (result != null) {
            log.info("resume from break-point manager is available.");
            available = true;
        }
        return result;
    }
    
    private static RegistryRepository createRegistryRepository(final GovernanceConfiguration config) {
        GovernanceCenterConfiguration registryCenterConfig = config.getRegistryCenterConfiguration();
        Preconditions.checkNotNull(registryCenterConfig, "Registry center configuration cannot be null.");
        RegistryRepository result = TypedSPIRegistry.getRegisteredService(RegistryRepository.class, registryCenterConfig.getType(), registryCenterConfig.getProps());
        result.init(config.getName(), registryCenterConfig);
        return result;
    }
}
