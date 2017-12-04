package eu.supersede.mdm.storage.cep.flume.interceptors;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.List;

public class EventNameInterceptor implements Interceptor {

    private String eventName;


    public EventNameInterceptor(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public void close() {

    }

    @Override
    public void initialize() {
        System.out.println("init " + eventName);
    }

    @Override
    public Event intercept(Event event) {

        event.getHeaders().put("EventName", eventName);

        return event;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        for (Event event : events) {

            intercept(event);
        }

        return events;
    }

    public static class Builder implements Interceptor.Builder {
        private String eventName;

        @Override
        public void configure(Context context) {
            // TODO Auto-generated method stub
            eventName = context.getString("eventName");

        }

        @Override
        public Interceptor build() {
            return new EventNameInterceptor(eventName);
        }
    }
}