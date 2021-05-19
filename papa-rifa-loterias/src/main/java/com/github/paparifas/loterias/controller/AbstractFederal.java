package com.github.paparifas.loterias.controller;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class AbstractFederal {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private JsonParser jsonParser = null;

	public AbstractFederal() {
		jsonParser = new JsonParser();
	}

	/**
	 * Realiza a chamada para a url
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected Response call(String url) throws IOException {
		return Jsoup.connect(url).execute();
	}

	/**
	 * Busca o numero do ultimo concurso
	 * @param body
	 * @return
	 */
	protected int getNumeroConcurso(String body) {
		if (body != null && body.trim().length() > 0) {
			JsonObject json = jsonParser.parse(body).getAsJsonObject();
			if (json.has("numero")) {
				return json.get("numero").getAsInt();
			}
		}
		return 0;
	}

	/**
	 * metodo para buscar o ultimo concurso salvo no elasticSearch
	 * 
	 * @return
	 */
	protected int getNumeroLastConcursoSaved() {
		// TODO: implemantar busca do ultimo concurso no elastic search
		return 0;
	}

	/**
	 * Verifica se o json não é vazio e se não deu erro na chamada
	 * Quando retorna erro eh pq nao tem o resultado do concurso
	 * 
	 * @param body
	 * @return
	 */
	protected boolean isSuccess(String body) {
		if (body != null && body.trim().length() > 0) {
			JsonObject json = jsonParser.parse(body).getAsJsonObject();

			if (json.has("erro")) {
				return false;
			} else {
				return true;
			}

		} else {
			return false;
		}
	}

	/**
	 * Verifica se o json não é vazio e se não deu erro na chamada
	 * 
	 * @param body
	 * @return
	 */
	protected boolean hasError(String body) {
		if (body != null && body.trim().length() > 0) {
			JsonObject json = jsonParser.parse(body).getAsJsonObject();

			if (!json.has("erro")) {
				return false;
			}
		}
		return true;
	}
	
	protected String getMensagem(String body) {
		if (body != null && body.trim().length() > 0) {
			JsonObject json = jsonParser.parse(body).getAsJsonObject();
			if (json.has("mensagem")) {
				return json.get("mensagem").getAsString();
			}
		}
		return null;
	}
}
