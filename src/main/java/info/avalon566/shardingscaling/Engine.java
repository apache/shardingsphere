package info.avalon566.shardingscaling;

import info.avalon566.shardingscaling.job.ScalingJob;
import info.avalon566.shardingscaling.job.schedule.standalone.InProcessScheduler;
import info.avalon566.shardingscaling.utils.RuntimeUtil;
import lombok.var;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

/**
 * @author avalon566
 */
public class Engine {

    private final static Logger LOGGER = LoggerFactory.getLogger(Engine.class);

    static {
        PropertyConfigurator.configure(RuntimeUtil.getBasePath() + File.separator + "conf" + File.separator + "log4j.properties");
    }

    public static void main(String[] args) {
        LOGGER.info("ShardingScaling Startup");
        var scheduler = new InProcessScheduler();
        if ("scaling".equals(args[0])) {
            new ScalingJob(Arrays.copyOfRange(args, 1, args.length), scheduler).run();
        }
    }
}