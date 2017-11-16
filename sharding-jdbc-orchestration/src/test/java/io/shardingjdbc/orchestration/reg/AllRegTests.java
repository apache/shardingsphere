package io.shardingjdbc.orchestration.reg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RegistryPathTest.class,
        EtcdOrchestratorTest.class,
        OrchestratorBuilderIntegrateTest.class
})
public class AllRegTests {
}
