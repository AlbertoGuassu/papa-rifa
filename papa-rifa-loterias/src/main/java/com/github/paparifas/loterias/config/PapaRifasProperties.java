package com.github.paparifas.loterias.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.paparifas.loterias.exception.PapaRifasConfigurationException;

public final class PapaRifasProperties {
	Logger logger = LoggerFactory.getLogger(PapaRifasProperties.class);
	
	public static final String PAPARIFAS_LOTERIAS_FEDERAL_URL_ALL = "paparifas.loterias.federal.url.all";
	public static final String PAPARIFAS_LOTERIAS_FEDERAL_URL_CONCURSO = "paparifas.loterias.federal.url.concurso";
	public static final String PAPARIFAS_LOTERIAS_FEDERAL_URL_LAST = "paparifas.loterias.federal.url.last";
	public static final String PAPARIFAS_LOTERIAS_FEDERAL_THREAD_NUMBER = "paparifas.loterias.federal.threadNumber";

	public static final String PAPARIFAS_LOTERIAS_FEDERAL_BOOTSTRAP_SERVERS_CONFIG = "paparifas.loterias.federal.bootstrap.servers";
	
	
	
	private static PapaRifasProperties instance = null;

	private Properties properties;

	private PapaRifasProperties() {
		this.loadProperties();
	}

	public static PapaRifasProperties instance() {
		if (instance == null) {
			instance = new PapaRifasProperties();
		}

		return instance;
	}

	private void loadProperties() {
		logger.debug("Iniciando a leitura do arquivo de configuração!");

		try {
			this.properties = new Properties();
			this.properties.load(PapaRifasProperties.class.getClassLoader().getResourceAsStream("application.properties"));
		} catch (Exception e) {
			logger.error("Erro ao carregar as propriedades!", e);
			throw new PapaRifasConfigurationException("Erro ao carregar as propriedades!", e);
		}
		
		logger.debug("Propriedades carregadas!");
		this.properties.forEach((k, v) -> logger.debug("Key : " + k + ", Value : " + v));
	}
	
	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}
}
