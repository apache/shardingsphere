import com.google.common.collect.Lists;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.orchestration.reg.base.ConfigChangeEvent;
import io.shardingjdbc.orchestration.reg.base.ConfigChangeListener;
import io.shardingjdbc.orchestration.reg.base.ConfigServer;
import io.shardingjdbc.orchestration.reg.etcd.EtcdConfigServer;
import io.shardingjdbc.orchestration.reg.etcd.EtcdConfiguration;
import lombok.val;

public class Main {
    public static void main(String[] args) throws Exception {
        final ConfigServer configServer = EtcdConfigServer.from(EtcdConfiguration.builder()
                .serverLists("http://localhost:2379")
                .namespace("/hawk/config/pms")
                .build());
        configServer.open();
        configServer.addConfigChangeListener(new ConfigChangeListener() {
            @Override
            public void onConfigChange(ConfigChangeEvent configChangeEvent) {
                System.out.print(configChangeEvent.getNewConfig());
            }
        });
        val masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig.setLoadBalanceAlgorithmClassName("abc");
        masterSlaveRuleConfig.setLoadBalanceAlgorithmType(MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN);
        masterSlaveRuleConfig.setMasterDataSourceName("ds1");
        masterSlaveRuleConfig.setName("ds");
        masterSlaveRuleConfig.setSlaveDataSourceNames(Lists.newArrayList("ds2", "ds3"));
        configServer.persistMasterSlaveRuleConfiguration(masterSlaveRuleConfig);

        val oldConfig = configServer.loadMasterSlaveRuleConfiguration();

        Thread.sleep(1000000);
    }
}
