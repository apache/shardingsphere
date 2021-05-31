package org.apache.shardingsphere.governance.core.registry.listener.factory;

import org.apache.shardingsphere.governance.core.registry.listener.GovernanceListener;
import org.apache.shardingsphere.governance.core.registry.listener.GovernanceListenerFactory;
import org.apache.shardingsphere.governance.core.registry.listener.impl.GlobalRuleChangedListener;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;

import java.util.Collection;
import java.util.Collections;

/**
 *  Global rule changed listener factory.
 */
public final class GlobalRuleChangedListenerFactory implements GovernanceListenerFactory {
    @Override
    public GovernanceListener create(final RegistryCenterRepository registryCenterRepository, final Collection<String> schemaNames) {
        return new GlobalRuleChangedListener(registryCenterRepository);
    }
    
    @Override
    public Collection<Type> getWatchTypes() {
        return Collections.singleton(Type.UPDATED);
    }
}
