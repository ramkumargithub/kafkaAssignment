
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AlarmProcessor {

    static final String JSON_SOURCE_TOPIC = "streams-json-input";
    static final String ALARM_DESTINATION_TOPIC = "streams-most-frequent-alarms";
    static AtomicInteger idGenerator = new AtomicInteger(1);
    static List<Alarm> alarmList = new ArrayList<>();

    public static void main(final String[] args) {
        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
        final String schemaRegistryUrl = args.length > 1 ? args[1] : "http://localhost:8081";
        final KafkaStreams streams = buildJsonToAvroStream(
                bootstrapServers,
                schemaRegistryUrl
        );
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    static KafkaStreams buildJsonToAvroStream(final String bootstrapServers,
                                              final String schemaRegistryUrl) {
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "json-to-avro-stream-conversion");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "json-to-avro-stream-conversion-client");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Where to find the Confluent schema registry instance(s)
        streamsConfiguration.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100 * 1000);

        final ObjectMapper objectMapper = new ObjectMapper();

        final StreamsBuilder builder = new StreamsBuilder();

        // read the source stream
        final KStream<String, String> jsonToAvroStream = builder.stream(JSON_SOURCE_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String()));
        jsonToAvroStream.mapValues(value -> {
            Alarm alarm = null;
            try {
                if (null != value && !value.isEmpty() && value.contains("metadata")) {
                    value = value.substring(value.indexOf(": \"") + 3, value.length() - 1);
                    value = value.replaceAll("\\\\", "");
                }
                final JsonNode jsonNode = objectMapper.readTree(value);
                alarm = new Alarm(idGenerator.getAndIncrement(), jsonNode.get("affectedNode").asText(),
                        jsonNode.get("vnocAlarmID").asText(), Timestamp.valueOf(jsonNode.get("alarmEventTime").asText()));
                alarmList.add(alarm);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            return alarmList;
        }).filter((k,v) -> v != null).to(ALARM_DESTINATION_TOPIC);

        saveStatisticsToText(alarmList);
        return new KafkaStreams(builder.build(), streamsConfiguration);
    }

    static void saveStatisticsToText(List<Alarm> alarmList){

        Timestamp oneHourAgo = new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000));
        Map<String, Long> groupByAlarmID = alarmList.stream().collect(Collectors.groupingBy(Alarm::getVnocAlarmID, Collectors.counting()));
        Map<String, Long> groupByNode = alarmList.stream().collect(Collectors.groupingBy(Alarm::getAffectedNode, Collectors.counting()));
        Long era015AlarmsTotal = alarmList.stream().filter(alarm -> (alarm.getVnocAlarmID()=="ERA015"))
                .count();
        Long era015AlarmInLastHour = alarmList.stream().filter(alarm -> (alarm.getVnocAlarmID()=="ERA015"))
                .filter(alarm -> (alarm.getAlarmEventTime().after(oneHourAgo)))
                .count();

        StringBuffer text= new StringBuffer("Alarms Statistics Data\"\\r\\n\"");
        text.append("Group By AlarmID \"\\r\\n\"");
        groupByAlarmID.forEach((k,v) -> text.append(k + " : count= " + v + "\"\\r\\n\""));
        text.append("Group By Node \"\\r\\n\"");
        groupByNode.forEach((k,v) -> text.append(k + " : count= " + v + "\"\\r\\n\""));
        text.append("ERA015 alarms Total \"\\r\\n\"");
        text.append(era015AlarmsTotal.toString());
        text.append("ERA015 alarms in last 1 Hour \"\\r\\n\"");
        text.append(era015AlarmInLastHour.toString());
        try {
            Files.write(Paths.get("./outputAlarmStatistics.txt"), text.toString().getBytes());
        } catch (final IOException e){
            throw new RuntimeException(e);
        }
    }

}