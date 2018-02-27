package com.albertodepaola.fileconsumer;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.albertodepaola.fileconsumer.async.DirectoryWatcherService;


public class FileConsumer {

	private static final String DATA_OUT = "/data/out";
	private static final String DATA_IN = "/data/in";

	public static void main(String[] args){

		
		String homePath = System.getenv("HOMEPATH");
		if(homePath == null || homePath.isEmpty()) {
			throw new IllegalArgumentException("HOMEPATH variable not setted.");
		}
		
		// TODO colocar em configuração
		// Toma a variavel de ambiente HOMEPATH e cria o diretorio de entrada e o de saida   
		String homePathIn = homePath + DATA_IN;
		String homePathOut = homePath + DATA_OUT;
				
		Path dirIn = Paths.get(homePathIn);
		Path dirOut = Paths.get(homePathOut);
		
		try {
			// cria e inicia o serviço de verificação do diretorio de entrada
			DirectoryWatcherService dws = new DirectoryWatcherService(dirIn, dirOut);
			dws.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
