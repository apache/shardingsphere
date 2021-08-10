package org.apache.shardingsphere.readwritesplitting.distsql.handler.converter;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public final class ReadwriteSplittingRuleStatementConverterTest {


    @Test
    public void convertTest() {
        ReadwriteSplittingRuleConfiguration emptyRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.emptyList());
        assertEmptyRuleSegmentConvertResult(emptyRuleSegmentConvertResult);
        Properties properties1 = new Properties();
        properties1.setProperty("ping", "pong");
        ReadwriteSplittingRuleSegment singleReadwriteSplittingRuleSegment = createReadwriteSplittingRuleSegment("name1", "write_ds_01", Arrays.asList("read_ds_01", "read_ds_02"), "lb01", properties1);
        ReadwriteSplittingRuleConfiguration singleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.singleton(singleReadwriteSplittingRuleSegment));
        assertSingleRuleSegmentConvertResult(singleReadwriteSplittingRuleSegment, singleRuleSegmentConvertResult);
        Properties properties2 = new Properties();
        properties2.setProperty("ping1", "pong1");
        Properties properties3 = new Properties();
        properties3.setProperty("ping2", "pong2");
        Properties properties4 = new Properties();
        properties4.setProperty("ping3", "pong3");
        properties4.setProperty("ping4", "pong4");
        List<ReadwriteSplittingRuleSegment> multipleReadwriteSplittingRuleSegments = Arrays.asList(createReadwriteSplittingRuleSegment("name2", "write_ds_02", Arrays.asList("read_ds_02", "read_ds_03"), "lb02", properties2),
                createReadwriteSplittingRuleSegment("name3", "write_ds_03", Arrays.asList("read_ds_04", "read_ds_05"), "lb03", properties3),
                createReadwriteSplittingRuleSegment("name4", "write_ds_04", Arrays.asList("read_ds_06", "read_ds_07"), "lb04", properties4));
        ReadwriteSplittingRuleConfiguration multipleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter.convert(multipleReadwriteSplittingRuleSegments);
        assertMultipleReadwriteSplittingRuleSegments(multipleReadwriteSplittingRuleSegments, multipleRuleSegmentConvertResult);
    }


    private ReadwriteSplittingRuleSegment createReadwriteSplittingRuleSegment(final String name,
                                                                              final String writeDataSource,
                                                                              final List<String> readDataSourceList,
                                                                              final String loadBalancerTypeName,
                                                                              final Properties properties) {
        return new ReadwriteSplittingRuleSegment(name, writeDataSource, readDataSourceList, loadBalancerTypeName, properties);
    }

    private void assertEmptyRuleSegmentConvertResult(ReadwriteSplittingRuleConfiguration emptyRuleSegmentConvertResult) {
        assertTrue(emptyRuleSegmentConvertResult.getDataSources().isEmpty());
        assertTrue(emptyRuleSegmentConvertResult.getLoadBalancers().isEmpty());
    }

    private void assertSingleRuleSegmentConvertResult(ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment, ReadwriteSplittingRuleConfiguration singleRuleSegmentConvertResult) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> singleRuleSegmentConvertResultDataSources = singleRuleSegmentConvertResult.getDataSources();
        Map<String, ShardingSphereAlgorithmConfiguration> singleRuleSegmentConvertResultLoadBalancers = singleRuleSegmentConvertResult.getLoadBalancers();
        assertEquals(1, singleRuleSegmentConvertResultDataSources.size());
        assertEquals(1, singleRuleSegmentConvertResultLoadBalancers.size());
        Object[] dataSources = singleRuleSegmentConvertResultDataSources.toArray();
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfiguration = (ReadwriteSplittingDataSourceRuleConfiguration) dataSources[0];
        String loadBalancerName = singleRuleSegmentConvertResultLoadBalancers.keySet().toArray()[0].toString();
        ShardingSphereAlgorithmConfiguration sphereAlgorithmConfiguration = (ShardingSphereAlgorithmConfiguration) singleRuleSegmentConvertResultLoadBalancers.values().toArray()[0];
        assertEquals(ruleConfiguration.getName(), readwriteSplittingRuleSegment.getName());
        assertThat(ruleConfiguration.getLoadBalancerName(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
        assertEquals(ruleConfiguration.getWriteDataSourceName(), readwriteSplittingRuleSegment.getWriteDataSource());
        assertEquals(ruleConfiguration.getReadDataSourceNames(), readwriteSplittingRuleSegment.getReadDataSources());
        assertThat(loadBalancerName, org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
        assertThat(sphereAlgorithmConfiguration.getType(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
        assertEquals(sphereAlgorithmConfiguration.getProps(), readwriteSplittingRuleSegment.getProps());
    }


    private void assertMultipleReadwriteSplittingRuleSegments(List<ReadwriteSplittingRuleSegment> readwriteSplittingRuleSegmentList, ReadwriteSplittingRuleConfiguration multipleReadwriteSplittingRuleSegments) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> singleRuleSegmentConvertResultDataSources = multipleReadwriteSplittingRuleSegments.getDataSources();
        Map<String, ShardingSphereAlgorithmConfiguration> singleRuleSegmentConvertResultLoadBalancers = multipleReadwriteSplittingRuleSegments.getLoadBalancers();
        assertEquals(readwriteSplittingRuleSegmentList.size(), singleRuleSegmentConvertResultDataSources.size());
        assertEquals(readwriteSplittingRuleSegmentList.size(), singleRuleSegmentConvertResultLoadBalancers.size());
        ReadwriteSplittingDataSourceRuleConfiguration[] ruleConfigurations = new ReadwriteSplittingDataSourceRuleConfiguration[readwriteSplittingRuleSegmentList.size()];
        singleRuleSegmentConvertResultDataSources.toArray(ruleConfigurations);
        Stream.iterate(0, i -> i + 1)
                .limit(readwriteSplittingRuleSegmentList.size())
                .forEach(i -> {
                    ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment = readwriteSplittingRuleSegmentList.get(i);
                    ReadwriteSplittingDataSourceRuleConfiguration ruleConfiguration = ruleConfigurations[i];
                    assertEquals(ruleConfiguration.getName(), readwriteSplittingRuleSegment.getName());
                    assertThat(ruleConfiguration.getLoadBalancerName(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
                    assertEquals(ruleConfiguration.getWriteDataSourceName(), readwriteSplittingRuleSegment.getWriteDataSource());
                    assertEquals(ruleConfiguration.getReadDataSourceNames(), readwriteSplittingRuleSegment.getReadDataSources());
                    assertTrue(singleRuleSegmentConvertResultLoadBalancers.containsKey(ruleConfiguration.getLoadBalancerName()));
                    ShardingSphereAlgorithmConfiguration sphereAlgorithmConfiguration = singleRuleSegmentConvertResultLoadBalancers.get(ruleConfiguration.getLoadBalancerName());
                    assertThat(sphereAlgorithmConfiguration.getType(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
                    assertEquals(sphereAlgorithmConfiguration.getProps(), readwriteSplittingRuleSegment.getProps());
                });
    }


}
