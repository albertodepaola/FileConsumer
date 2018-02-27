package com.albertodepaola.fileconsumer;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.albertodepaola.fileconsumer.async.DirectoryWatcherService;


public class FileConsumer {

	private static final String DATA_OUT = "/data/out";
	private static final String DATA_IN = "/data/in";

	public static void main(String[] args){

		// TODO move to config
		String homePath = System.getenv("HOMEPATH");
		String homePathIn = homePath + DATA_IN;
		String homePathOut = homePath + DATA_OUT;
		
		if(homePath == null || homePath.isEmpty()) {
			throw new IllegalArgumentException("HOMEPATH variable not setted.");
		}
		
		
		Path dirIn = Paths.get(homePathIn);
		Path dirOut = Paths.get(homePathOut);
		
		try {
			DirectoryWatcherService dws = new DirectoryWatcherService(dirIn, dirOut);
			dws.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
