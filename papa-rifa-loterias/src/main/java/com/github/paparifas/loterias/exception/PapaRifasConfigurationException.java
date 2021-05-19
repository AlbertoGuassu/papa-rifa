package com.github.paparifas.loterias.exception;

public class PapaRifasConfigurationException extends RuntimeException {

	public PapaRifasConfigurationException(String message, Exception cause) {
		super(message, cause);
	}

	public PapaRifasConfigurationException(String message) {
		super(message);
	}

	public PapaRifasConfigurationException(Throwable cause) {
		super(cause);
	}
}
