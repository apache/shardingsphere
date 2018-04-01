package io.shardingjdbc.core.merger.dal;

import io.shardingjdbc.core.merger.QueryResult;
import io.shardingjdbc.core.merger.dal.show.ShowCreateTableMergedResult;
import io.shardingjdbc.core.merger.dal.show.ShowDatabasesMergedResult;
import io.shardingjdbc.core.merger.dal.show.ShowOtherMergedResult;
import io.shardingjdbc.core.merger.dal.show.ShowTablesMergedResult;
import io.shardingjdbc.core.merger.fixture.TestQueryResult;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowCreateTableStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowOtherStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingjdbc.core.parsing.parser.sql.dal.DALStatement;
import io.shardingjdbc.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class DALMergeEngineTest {
    
    private ShardingRule shardingRule;
    
    private List<QueryResult> queryResults;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        queryResults = Collections.<QueryResult>singletonList(new TestQueryResult(resultSet));
    }
    
    @Test
    public void assertMergeForShowDatabasesStatement() throws SQLException {
        DALStatement dalStatement = new ShowDatabasesStatement();
        DALMergeEngine dalMergeEngine = new DALMergeEngine(shardingRule, queryResults, dalStatement);
        assertThat(dalMergeEngine.merge(), instanceOf(ShowDatabasesMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowShowTablesStatement() throws SQLException {
        DALStatement dalStatement = new ShowTablesStatement();
        DALMergeEngine dalMergeEngine = new DALMergeEngine(shardingRule, queryResults, dalStatement);
        assertThat(dalMergeEngine.merge(), instanceOf(ShowTablesMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowCreateTableStatement() throws SQLException {
        DALStatement dalStatement = new ShowCreateTableStatement();
        DALMergeEngine dalMergeEngine = new DALMergeEngine(shardingRule, queryResults, dalStatement);
        assertThat(dalMergeEngine.merge(), instanceOf(ShowCreateTableMergedResult.class));
    }
    
    @Test
    public void assertMergeForShowOtherStatement() throws SQLException {
        DALStatement dalStatement = new ShowOtherStatement();
        DALMergeEngine dalMergeEngine = new DALMergeEngine(shardingRule, queryResults, dalStatement);
        assertThat(dalMergeEngine.merge(), instanceOf(ShowOtherMergedResult.class));
    }
}
