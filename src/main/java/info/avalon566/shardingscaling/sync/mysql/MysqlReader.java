package info.avalon566.shardingscaling.sync.mysql;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.jdbc.AbstractJdbcReader;
import info.avalon566.shardingscaling.sync.jdbc.JdbcUri;
import lombok.var;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author avalon566
 */
public class MysqlReader extends AbstractJdbcReader {

    public MysqlReader(RdbmsConfiguration rdbmsConfiguration) {
        super(rdbmsConfiguration);
    }

    @Override
    public List<RdbmsConfiguration> split(int concurrency) {
        rdbmsConfiguration.setJdbcUrl(fixMysqlUrl(rdbmsConfiguration.getJdbcUrl()));
        return super.split(concurrency);
    }

    private String fixMysqlUrl(String url) {
        var uri = new JdbcUri(url);
        return String.format("jdbc:%s://%s/%s?%s", uri.getScheme(), uri.getHost(), uri.getDatabase(), fixMysqlParams(uri.getParameters()));
    }

    private String formatMysqlParams(Map<String, String> params) {
        var result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(entry.getKey());
            if (null != entry.getValue()) {
                result.append("=").append(entry.getValue());
            }
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    private String fixMysqlParams(HashMap<String, String> parameters) {
        if (!parameters.containsKey("yearIsDateType")) {
            parameters.put("yearIsDateType", "false");
        }
        return formatMysqlParams(parameters);
    }
}