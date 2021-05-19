package com.github.paparifas.loterias.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jsoup.Connection.Response;

import com.github.paparifas.loterias.config.PapaRifasProperties;
import com.github.paparifas.loterias.producer.FederalRunnable;
import com.github.paparifas.loterias.producer.PapaRifasProducer;

public class ProducerController extends AbstractFederal {
	
	public ProducerController() {
		
	}
	
	public void run() {
		logger.debug("Iniciando o processamento da loteria federal!");
		Instant start = Instant.now();
		
		//iniciando o processamento
		try {
			this.loadMissing();
		} catch (IOException e) {
			logger.error("Ocorreu um erro ao realizar a carga de concursos!", e);
		}
		
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).getSeconds();
		logger.debug("Finalizando o processamento da loteria federal!");
		logger.info("Execution time: {} segundos", timeElapsed);
	}
	
	public int loadLastConcurso() throws IOException {
		logger.debug("Buscando o numero do ultimo concurso!");
		try {
			String url = PapaRifasProperties.instance().getProperty(PapaRifasProperties.PAPARIFAS_LOTERIAS_FEDERAL_URL_LAST);
			Response response = this.call(url);
			int concurso = this.getNumeroConcurso(response.body());
			logger.info("Numero do ultimo concurso eh: {}", concurso);
			return concurso;
		} catch (IOException e) {
			logger.error("Erro ao buscar o ultimo concurso!");
			throw e; 
		}
	}
	
	public void loadMissing() throws IOException {
		logger.debug("Iniciando a leitura dos concursos faltantes!");
		
		//Busca o ultimo concurso salvo
		int lastSaved = this.getNumeroLastConcursoSaved();
		
		//Buscar o ultimo concurso realizado pela loteria federal
		int lastConcurso = this.loadLastConcurso();
		
		String url = PapaRifasProperties.instance().getProperty(PapaRifasProperties.PAPARIFAS_LOTERIAS_FEDERAL_URL_CONCURSO);
		Integer threadNumber = Integer.valueOf(PapaRifasProperties.instance().getProperty(PapaRifasProperties.PAPARIFAS_LOTERIAS_FEDERAL_THREAD_NUMBER));
		
		//Criando um executor com o numero de treads especificadas
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber);
		
		PapaRifasProducer producer = new PapaRifasProducer();
		Thread producerThread = new Thread(producer);
		
		
		logger.info("Ira realizar a leitura do concurso {} ate {}, utilizando {} threads!", lastSaved, lastConcurso, threadNumber);
		for (int concurso = lastSaved; concurso <= lastConcurso; concurso++) {
			String urlConcurso = url.replaceAll("\\{NUMERO_CONCURSO\\}", String.valueOf(concurso));
			FederalRunnable command = new FederalRunnable(urlConcurso, concurso, producer);
			executor.execute(command);			
		}
		
		producerThread.start();		
		//indica pro executor que pode dar shutdown
		executor.shutdown();
		while(!executor.isTerminated()) {
			try {
				logger.info(
	                    String.format("[ThreadPoolExecutor] [%d/%d] Active: %d, Complete: %d, Task: %d, isShutdown: %s, isTerminated: %s",
	                        executor.getPoolSize(),
	                        executor.getCorePoolSize(),
	                        executor.getActiveCount(),
	                        executor.getCompletedTaskCount(),
	                        executor.getTaskCount(),
	                        executor.isShutdown(),
	                        executor.isTerminated()));
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
		
		//informa o producer que ja acabou de ler todos os concursos
		producer.setRunning(false);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.debug("Finalizando a aplicação");
			producer.closeProducer();
			producerThread.interrupt();
		}));
	}
}
