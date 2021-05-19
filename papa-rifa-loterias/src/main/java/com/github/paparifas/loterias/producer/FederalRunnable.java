package com.github.paparifas.loterias.producer;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paparifas.loterias.controller.AbstractFederal;
import com.github.paparifas.loterias.main.MensagemFederal;

public class FederalRunnable extends AbstractFederal implements Runnable {
	private Logger logger = LoggerFactory.getLogger(FederalRunnable.class);
	private String url;
	private Integer concurso;
	private PapaRifasProducer producer;

	public FederalRunnable(String url, Integer concurso, PapaRifasProducer producer) {
		this.url = url;
		this.concurso = concurso;
		this.producer = producer;
	}

	@Override
	public void run() {
		logger.debug("Iniciando o processamento do concurso {}!", concurso);
		this.process();
		logger.debug("Finalizando o processamento do concurso {}!", concurso);

	}

	public void process() {
		Response execute = null;
		try {
			execute = Jsoup.connect(this.url).execute();

			// o erro eh quando nao existe resultado para o concurso informado
			if (!this.hasError(execute.body())) {
				this.sendResultado(execute.body());
			} else {
				this.sendConcursoSemResultado(this.getMensagem(execute.body()));
			}
		} catch (IOException e) {
			this.sendError(e.getMessage());
		}
	}

	private void sendResultado(String json) {
		logger.debug("Realizar a chamada para informar o resultado");
		producer.sendMensageResultado(new MensagemFederal(String.valueOf(concurso), json));
		
	}

	private void sendConcursoSemResultado(String detail) {
		logger.debug("Nao foi possivel carregar o concurso {}", concurso);
		
		StringBuilder msg = new StringBuilder();
		msg.append("{");
		msg.append("\"tipoJogo\": \"LOTERIA_FEDERAL\",");
		msg.append("\"numero\":").append(concurso).append(",");
		msg.append("\"url\": \"").append(url).append("\",");
		msg.append("\"status\": \"NOT_FOUND\",");
		msg.append("\"detail\":\"").append(detail).append("\""); 
		msg.append("}");
		producer.sendMensageNotFound(new MensagemFederal(String.valueOf(concurso), msg.toString()));
	}

	private void sendError(String detail) {
		logger.debug("Nao foi encontrado nenhum resultado para o concurso {}", concurso);
		StringBuilder msg = new StringBuilder();
		msg.append("{");
		msg.append("\"tipoJogo\": \"LOTERIA_FEDERAL\",");
		msg.append("\"numero\":").append(concurso).append(",");
		msg.append("\"url\": \"").append(url).append("\",");
		msg.append("\"status\": \"ERROR\",");
		msg.append("\"detail\":\"").append(detail).append("\""); 
		msg.append("}");
		producer.sendMensageError(new MensagemFederal(String.valueOf(concurso), msg.toString()));
	}

}
