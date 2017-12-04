package eu.supersede.mdm.storage.cep.flume.sinks;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**

 */
public class MongoSink extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(MongoSink.class);


    String hostname = "localhost";
    int port = 27017;
    String myMachineID = "localhost";
    DBCollection table;
    DB db;
    MongoClient mongo;

    @Override
    public synchronized void start() {
        super.start();

        try {
            /**** Connect to MongoDB ****/
            // Since 2.10.0, uses MongoClient
            mongo = new MongoClient(hostname, port);

            /**** Get database ****/
            // if database doesn't exists, MongoDB will create it for you
            db = mongo.getDB("statistics");

            /**** Get collection / table from 'testdb' ****/
            // if collection doesn't exists, MongoDB will create it for you
            table = db.getCollection("cep");

        } catch (Exception e) {
            System.out.println(e.toString());
            logger.error("can't process CEPElements, drop it!", e);
        }
    }

    @Override
    public void configure(Context context) {
        this.hostname = context.getString("hostname");
        this.port = context.getInteger("port");
        this.myMachineID = context.getString("myMachineID");
    }


    @Override
    public synchronized void stop() {
        super.stop();
    }

    @Override
    public Status process() throws EventDeliveryException {

        Status status = Status.READY;
        Channel channel = getChannel();
        Transaction tx = null;
        try {
            tx = channel.getTransaction();
            tx.begin();

            Event event = channel.take();

            if (event != null) {

                Map<String, String> headers = event.getHeaders();

                for (String key : headers.keySet()) {
                    System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ Key: " + key + ", Header: " + headers.get(key));
                }

//                String line = EventHelper.dumpEvent(event);
//
//                logger.debug(line);
//                String eventName = headers.get("EventName");
//                byte[] body = event.getBody();
//                System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ Body: " + new String(body));
//
//
//


                /**** Insert ****/
                // create a document to store key and value
                BasicDBObject document = new BasicDBObject();
                document.put("_id", myMachineID + System.currentTimeMillis());
                for (String key : headers.keySet()) {
                    document.put(key, headers.get(key));
                }

                table.insert(document);


            } else {
                status = Status.BACKOFF;
            }

            tx.commit();
        } catch (Exception e) {
            System.out.println(e.toString());
            logger.error("can't process CEPElements, drop it!", e);
            if (tx != null) {
                tx.commit();// commit to drop bad event, otherwise it will enter dead loop.
            }

            throw new EventDeliveryException(e);
        } finally {
            if (tx != null) {
                tx.close();
            }
        }
        return status;
    }
}