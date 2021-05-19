package com.github.paparifas.loterias.consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paparifas.loterias.config.PapaRifasProperties;

public class ConsumerRunnable implements Runnable{
	private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);
	private KafkaConsumer<String, String> consumer;
	
	public ConsumerRunnable() {
		this.initConsumer();
	}
	
	private void initConsumer() {
		Properties properties = new Properties();
		String bootstrapServers = PapaRifasProperties.instance().getProperty(PapaRifasProperties.PAPARIFAS_LOTERIAS_FEDERAL_BOOTSTRAP_SERVERS_CONFIG);

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "papa-rifas-cosumer-group");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Arrays.asList("loteria-federal-resultado", "loteria-federal-notfound", "loteria-federal-error"));
	}

	@Override
	public void run() {
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            
            BulkRequest bulkRequest = new BulkRequest();
            for (ConsumerRecord<String, String> record : records) {
                logger.info("Key: " + record.key() + ", Topic: " + record.topic() + ", Value: " + record.value());
                
                IndexRequest request = new IndexRequest();
                
                if ("loteria-federal-resultado".equals(record.topic())) {
                	request.index("resultado");
                } else if ("loteria-federal-notfound".equals(record.topic())) {
                	request.index("notfound");
                } else if ("loteria-federal-error".equals(record.topic())) {
                	request.index("error");
                } else {
                	continue;
                }
                
                request.id(record.key());
                request.source(record.value(),  XContentType.JSON);
                
                bulkRequest.add(request);
            }
            
            if (records.count() > 0) {
	            RestHighLevelClient client = this.createClient();
	            try {
					client.bulk(bulkRequest, RequestOptions.DEFAULT);
					logger.info("Enviado {} para ES.", records.count());
				} catch (IOException e) {
				}
            }
            
        }
		
	}
	
	
	private RestHighLevelClient createClient() {
		RestClientBuilder builder = RestClient.builder(new HttpHost("127.0.0.1", 9200, "http"));
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}
	
	public void shutdown() {
		consumer.wakeup();
	}
	
}
