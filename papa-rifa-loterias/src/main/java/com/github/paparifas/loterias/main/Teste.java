package com.github.paparifas.loterias.main;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

public class Teste {

	public static void main(String[] args) {
		RestHighLevelClient client = createClient();
		
		
		IndexRequest request = new IndexRequest("loteria-federal");
		request.type("testetype");
		request.source("{\"teste\":\"testando\"}", XContentType.JSON);
		try {
			client.index(request, RequestOptions.DEFAULT);
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	private static RestHighLevelClient createClient() {
		RestClientBuilder builder = RestClient.builder(new HttpHost("127.0.0.1", 9200, "http"));
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}
}
