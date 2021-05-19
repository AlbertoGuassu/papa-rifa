package com.github.paparifas.loterias.main;

public class MensagemFederal {
	private String key;
	private String mensage;
	
	public MensagemFederal(String key, String mensage) {
		super();
		this.key = key;
		this.mensage = mensage;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getMensage() {
		return mensage;
	}
	public void setMensage(String mensage) {
		this.mensage = mensage;
	}
}
