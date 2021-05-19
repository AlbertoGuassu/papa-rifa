package com.github.paparifas.loterias.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paparifas.loterias.config.PapaRifasProperties;
import com.github.paparifas.loterias.consumer.ConsumerRunnable;

public class ConsumerController {
	private Logger logger = LoggerFactory.getLogger(ConsumerController.class);
	
	public void run() {
		logger.debug("Iniciando os consumidores!");
		Integer threadNumber = Integer.valueOf(PapaRifasProperties.instance().getProperty(PapaRifasProperties.PAPARIFAS_LOTERIAS_FEDERAL_THREAD_NUMBER));
		List<Thread> cosumerThreads = new ArrayList<Thread>(threadNumber);
		
		for(int i = 0; i < threadNumber; i++) {
			Thread t = new Thread(new ConsumerRunnable());
			t.start();
			cosumerThreads.add(t);
		}
	
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.debug("Finalizando a aplicação");
			for (Thread thread : cosumerThreads) {
				thread.interrupt();
			}
		}));
	}

}
