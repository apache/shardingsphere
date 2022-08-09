package org.apache.shardingsphere.scaling.cor.job.importer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureImporter;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.importer.DefaultImporter;
import org.apache.shardingsphere.data.pipeline.spi.importer.Importer;
import org.apache.shardingsphere.scaling.core.job.importer.ImporterCreatorFactory;
import org.junit.Test;
import org.mockito.Mock;

public final class ImporterCreatorFactoryTest {

    @Mock
    private PipelineDataSourceManager dataSourceManager;

    @Mock
    private PipelineChannel channel;

    private final PipelineDataSourceConfiguration dataSourceConfig = new StandardPipelineDataSourceConfiguration(
            "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL;USER=root;PASSWORD=root", "root", "root");

    @Test
    public void assertCreateImporterForMysql() {
        Importer importer = ImporterCreatorFactory.getInstance("MySQL")
                .createImporter(mockImporterConfiguration(), dataSourceManager, channel,
                        new FixturePipelineJobProgressListener());
        assertThat(importer, instanceOf(DefaultImporter.class));
    }

    @Test
    public void assertCreateImporterForPostgreSQL() {
        Importer importer = ImporterCreatorFactory.getInstance("PostgreSQL")
                .createImporter(mockImporterConfiguration(), dataSourceManager, channel,
                        new FixturePipelineJobProgressListener());
        assertThat(importer, instanceOf(DefaultImporter.class));
    }

    @Test
    public void assertCreateImporterForOpenGauss() {
        Importer importer = ImporterCreatorFactory.getInstance("openGauss")
                .createImporter(mockImporterConfiguration(), dataSourceManager, channel,
                        new FixturePipelineJobProgressListener());
        assertThat(importer, instanceOf(DefaultImporter.class));
    }

    @Test
    public void assertCreateImporterForH2() {
        Importer importer = ImporterCreatorFactory.getInstance("H2")
                .createImporter(mockImporterConfiguration(), dataSourceManager, channel,
                        new FixturePipelineJobProgressListener());
        assertThat(importer, instanceOf(FixtureImporter.class));
    }

    private ImporterConfiguration mockImporterConfiguration() {
        Map<LogicTableName, Set<String>>
                shardingColumnsMap = Collections.singletonMap(new LogicTableName("test_table"), Collections.singleton("user"));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, new TableNameSchemaNameMapping(Collections.emptyMap()), 1000, 3, 3);
    }
}
