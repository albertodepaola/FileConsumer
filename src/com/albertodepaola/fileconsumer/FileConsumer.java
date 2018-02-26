package com.albertodepaola.fileconsumer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.albertodepaola.fileconsumer.async.DirectoryWatcherService;


public class FileConsumer {

	public static void main(String[] args){

		// TODO move to config
		String homePath = System.getenv("HOMEPATH");
		String homePathIn = homePath + "/data/in";
		String homePathOut = homePath + "/data/out";
		
		if(homePath == null || homePath.isEmpty()) {
			throw new IllegalArgumentException("HOMEPATH variable not setted.");
		}
		
		
		Path dirIn = Paths.get(homePathIn);
		Path dirOut = Paths.get(homePathOut);
		
		try {
			DirectoryWatcherService dws = new DirectoryWatcherService(dirIn, dirOut);
			dws.start();
			// TimeUnit.SECONDS.sleep(10);
			// dws.stop();
			// System.out.println("stopped");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
