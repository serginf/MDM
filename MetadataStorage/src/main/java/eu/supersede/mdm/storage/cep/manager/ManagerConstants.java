package eu.supersede.mdm.storage.cep.manager;

/**
 * Created by osboxes on 30/05/17.
 */
public class ManagerConstants {
    public static final String TYPE_INT = "int";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_BYTES = "bytes";
    public static final String TYPE_LONG = "long";

    public static final String DOT = ".";
    public static final String SOURCES = "sources";
    public static final String CHANNELS = "channels";
    public static final String SINKS = "sinks";
    public static final String SOURCES_PREFIX = SOURCES + DOT;
    public static final String CHANNELS_PREFIX = CHANNELS + DOT;
    public static final String SINKS_PREFIX = SINKS + DOT;

    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String EVENT_PREFIX = "event.";

    public static final String SOURCE_EVENT_TYPE = EVENT_PREFIX + TYPE;
    public static final String SOURCE_EVENT_TYPE_JSON = "json";
    public static final String SOURCE_EVENT_TYPE_XML = "xml";
    public static final String SOURCE_EVENT_NAME = EVENT_PREFIX + NAME;
    public static final String CUSTOM_KAFKA_SOURCE_INSTANCE = "upc.edu.cep.flume.sources.CEPKafkaSource";
    public static final String SOURCE_ATTRIBUTES = "attributes";
    public static final String SOURCE_KAFKA_BOOTSTRAP = "kafka.bootstrap.servers";
    public static final String SOURCE_TOPIC = "topic";
    public static final String SOURCE_BATCH_SIZE = "batchSize";
    public static final String INTERCEPTORS = "interceptors";
    public static final String INTERCEPTORS_PREFIX = "interceptors.";
    public static final String Distributed_INTERCEPTOR_PREFIX = INTERCEPTORS_PREFIX + "DistributedInterceptor.";
    public static final String Distributed_INTERCEPTOR_EVENTNAME = Distributed_INTERCEPTOR_PREFIX + "eventName.";
    public static final String Distributed_INTERCEPTOR_TYPE = Distributed_INTERCEPTOR_PREFIX + TYPE;
    public static final String Distributed_INTERCEPTOR_TYPE_INSTANCE = "upc.edu.cep.flume.interceptors.DistributedInterceptor$Builder";
    public static final String TIMESTAMP_INTERCEPTOR_PREFIX = INTERCEPTORS_PREFIX + "TimestampInterceptor.";
    public static final String TIMESTAMP_INTERCEPTOR_TYPE = TIMESTAMP_INTERCEPTOR_PREFIX + TYPE;
    public static final String TIMESTAMP_INTERCEPTOR_TYPE_INSTANCE = "org.apache.flume.interceptor.TimestampInterceptor$Builder";
    public static final String HOST_INTERCEPTOR_PREFIX = INTERCEPTORS_PREFIX + "HostInterceptor.";
    public static final String HOST_INTERCEPTOR_TYPE = HOST_INTERCEPTOR_PREFIX + TYPE;
    public static final String HOST_INTERCEPTOR_TYPE_INSTANCE = "upc.edu.cep.flume.interceptors.HostInterceptor$Builder";
    public static final String HOST_INTERCEPTOR_PRESERVEEXISTING = HOST_INTERCEPTOR_PREFIX + "preserveExisting";
    public static final boolean HOST_INTERCEPTOR_PRESERVEEXISTING_INSTANCE = false;
    public static final String HOST_INTERCEPTOR_HOSTHEADER = HOST_INTERCEPTOR_PREFIX + "hostHeader";
    public static final String HOST_INTERCEPTOR_HOSTHEADER_INSTANCE = "hostname";
    public static final String SELECTOR_PREFIX = "selector.";
    public static final String SELECTOR_TYPE = SELECTOR_PREFIX + TYPE;
    public static final String SELECTOR_CHANNELS = SELECTOR_PREFIX + CHANNELS;

    public static final String SELECTOR_EQ = "eq";
    public static final String SELECTOR_NE = "ne";
    public static final String SELECTOR_GE = "ge";
    public static final String SELECTOR_GT = "gt";
    public static final String SELECTOR_LE = "le";
    public static final String SELECTOR_LT = "lt";

    public static final String CHANNEL_TPYE_MEMORY = "memory";
    public static final String CHANNEL_CAPACITY = "capacity";
    public static final String CHANNEL_TRANSACTION_CAPACITY = "transactionCapacity";
    public static final String CHANNEL_KEEP_ALIVE = "keep-alive";

    static final String CEP_SINK_EVENT_NAMES = "event.names";
    static final String CEP_SINK_TYPE = TYPE;
    static final String CEP_SINK_TYPE_INSTANCE = "upc.edu.cep.flume.sinks.CEPSinkOldVersion";
    static final String CEP_SINK_DELETED_RULES = "deletedRules";
    static final String CEP_SINK_EXPRESSION = "expression";
    static final String CEP_SINK_ACTIONS = "event.actions";
    static final String CEP_SINK_CHANNEL = "channel";
    static final String CEP_SINK_EVENT_ATTRIBUTES = "attributes";
    static final String CEP_SINK_RESTART = "restart";
    static final String CEP_SINK_RULE_ID = "ruleID";
}
