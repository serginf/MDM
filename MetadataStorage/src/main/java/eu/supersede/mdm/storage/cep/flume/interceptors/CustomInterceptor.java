package eu.supersede.mdm.storage.cep.flume.interceptors;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.List;

public class CustomInterceptor implements Interceptor {

    private String aaa;


    public CustomInterceptor(String aaa) {
        this.aaa = aaa;
        System.out.println(aaa);
    }

    @Override
    public void close() {

    }

    @Override
    public void initialize() {
        System.out.println("init " + aaa);
    }

    @Override
    public Event intercept(Event event) {
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
        private String aaa;

        @Override
        public void configure(Context context) {
            // TODO Auto-generated method stub
            aaa = context.getString("aaa");

        }

        @Override
        public Interceptor build() {
            return new CustomInterceptor(aaa);
        }
    }
}