package com.albertodepaola.fileconsumer.async;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.albertodepaola.fileconsumer.model.Tuple;
import com.albertodepaola.logparser.Parser;
import com.albertodepaola.logparser.factory.ABParserFactory;
import com.albertodepaola.logparser.factory.ParserFactory;
import com.albertodepaola.logparser.model.ABParserResult;
import com.albertodepaola.logparser.model.Configuration;
import com.albertodepaola.logparser.model.ParserResult;

public class FileExecutorService {

	static int count = 0;
	private static ExecutorService executor;
	
	public static void main(String[] args) {
//		ExecutorService executor = Executors.newFixedThreadPool(3);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
		    String threadName = Thread.currentThread().getName();
		    System.out.println("Hello " + threadName);
		    try {
				TimeUnit.SECONDS.sleep(2);
				System.out.println("After sleep");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		executor.shutdown();
		
		System.out.println(ForkJoinPool.getCommonPoolParallelism()); 
		
		
		
	}
	
	private static Callable<Tuple<String, File>> callable(File fileToParse) {
		return () -> {
			String status = "OK";
			try {
				Map<String, String> argumentsMap = new HashMap<>();
				
				argumentsMap.put("accesslog", fileToParse.getAbsolutePath());
				// TODO solve config issue coupling
				Configuration.loadConfigurationFromJsonFile("config.json");
			
				Parser<ABParserResult> abparser = new ParserFactory<ABParserResult>().createParser(new ABParserFactory(), argumentsMap);
				
				ParserResult<ABParserResult> parse = abparser.parse();
				
				String name = fileToParse.getName();
				
				System.out.println(name +" " + parse.getResult().getIdMaiorVenda());
				System.out.println(name +" " + parse.getResult().getQuantidadeDeClientes());
				System.out.println(name +" " + parse.getResult().getQuantidadeDeVendedores());
				System.out.println(name +" " + parse.getResult().getWorstSeller());
			
			} catch (Exception e) {
				e.printStackTrace();
				status = "NOK";
			}
			
			return new Tuple<String, File>(status, fileToParse);
		};
	}


	public static Future<Tuple<String, File>> submitFile(File file) {
		
		Callable<Tuple<String, File>> c = callable(file);

		ExecutorService ex = getExecutor();
		
		Future<Tuple<String, File>> submit = ex.submit(c);
		
		return submit;
		
	}

	private static ExecutorService getExecutor() {
		if(executor == null) {
			executor = Executors.newWorkStealingPool();
		}
		return executor;
	}
	
}
