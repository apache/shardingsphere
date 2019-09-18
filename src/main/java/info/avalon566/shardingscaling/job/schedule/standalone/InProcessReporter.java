package info.avalon566.shardingscaling.job.schedule.standalone;

import info.avalon566.shardingscaling.job.schedule.Event;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import lombok.var;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author avalon566
 */
public class InProcessReporter implements Reporter {

    private ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void report(Event event) {
        queue.offer(event);
    }

    @Override
    public Event consumeEvent() {
        while (true) {
            var event = queue.poll();
            if (null != event) {
                return event;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
