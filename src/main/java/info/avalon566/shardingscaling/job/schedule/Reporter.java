package info.avalon566.shardingscaling.job.schedule;

/**
 * @author avalon566
 */
public interface Reporter {

    void report(Event event);

    Event consumeEvent();
}
