package org.apache.shardingsphere.governance.core.facade;

import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class RegistryCenterRepositoryFactoryTest {

    @Test
    public void assertNewInstance() {
        GovernanceConfiguration config = new GovernanceConfiguration("test_name", new RegistryCenterConfiguration("TEST", "127.0.0.1", new Properties()), false);
        RegistryCenterRepository registryCenterRepository = RegistryCenterRepositoryFactory.newInstance(config);
        assertNotNull(registryCenterRepository);
        assertEquals("TEST", registryCenterRepository.getType());
    }
}
