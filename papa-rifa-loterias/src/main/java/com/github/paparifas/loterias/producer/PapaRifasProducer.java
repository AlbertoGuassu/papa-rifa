package com.github.paparifas.loterias.producer;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paparifas.loterias.config.PapaRifasProperties;
import com.github.paparifas.loterias.main.MensagemFederal;

public class PapaRifasProducer implements Runnable {
	private Logger logger = LoggerFactory.getLogger(PapaRifasProducer.class);
	private List<MensagemFederal> mensagensResultado = null;
	private List<MensagemFederal> mensagensError = null;
	private List<MensagemFederal> mensagensNotFound = null;

	private boolean running;

	private KafkaProducer<String, String> producer;

	public PapaRifasProducer() {
		this.mensagensResultado = new Vector<MensagemFederal>();
		this.mensagensError = new Vector<MensagemFederal>();
		this.mensagensNotFound = new Vector<MensagemFederal>();
		this.running = true;
		this.createKafkaProducer();
	}

	public void sendMensageResultado(MensagemFederal msg) {
		this.mensagensResultado.add(msg);
	}

	public void sendMensageError(MensagemFederal msg) {
		this.mensagensError.add(msg);
	}

	public void sendMensageNotFound(MensagemFederal msg) {
		this.mensagensNotFound.add(msg);
	}

	@Override
	public void run() {
		logger.info("Iniciando o processamento das mensagens!");
		while (isRunning() || !isEmpty()) {
			
				// se tiver alguma mensagem envia para o kafka
			if (!this.mensagensResultado.isEmpty()) {
				MensagemFederal msg = this.mensagensResultado.remove(0);
				this.sendToKafka("loteria-federal-resultado", msg);
			}

			// se tiver alguma mensagem envia para o kafka
			if (!this.mensagensError.isEmpty()) {
				MensagemFederal msg = this.mensagensError.remove(0);
				this.sendToKafka("loteria-federal-error", msg);
			}
			// se tiver alguma mensagem envia para o kafka
			if (!this.mensagensNotFound.isEmpty()) {
				MensagemFederal msg = this.mensagensNotFound.remove(0);
				this.sendToKafka("loteria-federal-notfound", msg);
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	private void sendToKafka(String topic, MensagemFederal msg) {
		logger.info("Enviando mensagem para topico {}", topic);
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, msg.getKey(), msg.getMensage());
		producer.send(record);
		producer.flush();
	}

	public void createKafkaProducer() {
		String bootstrapServers = PapaRifasProperties.instance().getProperty(PapaRifasProperties.PAPARIFAS_LOTERIAS_FEDERAL_BOOTSTRAP_SERVERS_CONFIG);

		// create Producer properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// create safe Producer
//        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
//        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
//        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
//        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); 

		// high throughput producer (at the expense of a bit of latency and CPU usage)
		properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024)); // 32 KB batch size

		// create the producer
		producer = new KafkaProducer<String, String>(properties);
	}

	public void closeProducer() {
		if (producer != null) {
			producer.close();
		}
	}
	
	private boolean isEmpty() {
		return this.mensagensResultado.isEmpty() && this.mensagensNotFound.isEmpty() && this.mensagensError.isEmpty();
	}
}