package com.github.paparifas.loterias.main;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paparifas.loterias.controller.ConsumerController;
import com.github.paparifas.loterias.controller.ProducerController;

public class ApplicationLoterias {
	Logger logger = LoggerFactory.getLogger(ApplicationLoterias.class);
	private static boolean isConsumer, isProducer;
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Paramentros invalidos!");
			System.out.println("USE: java -jar PapaRifasLoterias.jar [consumer] [producer]");
			return;
		}
		
		for (String mode: args) {
			if ("consumer".equalsIgnoreCase(mode)) {
				isConsumer = true;
			} else if ("producer".equalsIgnoreCase(mode)) {
				isProducer = true;
			}
		}
		
		Instant start = Instant.now();
		ApplicationLoterias applicationLoterias = new ApplicationLoterias();
		applicationLoterias.run();
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		LoggerFactory.getLogger(ApplicationLoterias.class).info("Execution time: {} ms", timeElapsed);
	}

	public void run() {
		logger.info("Inicializando a ApplicationLoterias!");
		if (isConsumer) {
			ConsumerController consumer =  new ConsumerController();
			consumer.run();
		}

		if (isProducer) {
			ProducerController producer =  new ProducerController();
			producer.run();
		}
		
		logger.info("Finalizando a ApplicationLoterias!");
	}
}
