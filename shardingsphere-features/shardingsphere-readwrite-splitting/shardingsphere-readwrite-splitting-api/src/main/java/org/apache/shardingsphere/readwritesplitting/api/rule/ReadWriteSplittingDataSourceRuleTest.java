package org.apache.shardingsphere.readwritesplitting.api.rule;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RequiredArgsConstructor
@Getter
public class ReadWriteSplittingDataSourceRuleTest {

    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfig;
    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfigDynamic;

    @Before
    public void setup(){
        readwriteSplittingDataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Static", getProperties("write_ds", "read_ds_0,read_ds_1"),"");
        readwriteSplittingDataSourceRuleConfigDynamic = new ReadwriteSplittingDataSourceRuleConfiguration("ds", "Dynamic", getProperties("write_ds", "read_ds_0,read_ds_1"),"");
    }

    @Test
    public void assertGetAutoAwareDataSourceName(){

        ReadwriteSplittingDataSourceRuleConfiguration anotherInstance = new ReadwriteSplittingDataSourceRuleConfiguration("ds","Dynamic",getProperties("write_ds","read_ds_0,read_ds_1"),"");
        String actual = readwriteSplittingDataSourceRuleConfigDynamic.getProps().getProperty("auto-aware-data-source-name");
        String testing = anotherInstance.getProps().getProperty("auto-aware-data-source-name");
        assertThat(testing,is(actual));
    }

    @Test
    public void assertGetWriteDataSourceName(){
        ReadwriteSplittingDataSourceRuleConfiguration anotherInstance = new ReadwriteSplittingDataSourceRuleConfiguration("ds","Static",getProperties("write_ds","read_ds_0,read_ds_1"),"");
        String actual = readwriteSplittingDataSourceRuleConfig.getProps().getProperty("write-data-source-name");
        String testing = anotherInstance.getProps().getProperty("write-data-source-name");
        assertThat(testing,is(actual));
    }

    @Test
    public void assertGetReadDataSourceNames(){
        ReadwriteSplittingDataSourceRuleConfiguration anotherInstance = new ReadwriteSplittingDataSourceRuleConfiguration("ds","Static",getProperties("write_ds","read_ds_0,read_ds_1"),"");
        assertThat(anotherInstance.getProps().getProperty("read-data-source-names"),is(Arrays.asList("read_ds_0", "read_ds_1")));
    }

    private Properties getProperties(final String writeDataSource, final String readDataSources) {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", writeDataSource);
        result.setProperty("read-data-source-names", readDataSources);
        return result;
    }
}
