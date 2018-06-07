package com.albertodepaola.fileconsumer.async;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.albertodepaola.fileconsumer.model.Status;
import com.albertodepaola.fileconsumer.model.Tuple;
import com.albertodepaola.logparser.Parser;
import com.albertodepaola.logparser.factory.ABParserFactory;
import com.albertodepaola.logparser.factory.ParserFactory;
import com.albertodepaola.logparser.model.ABParserResult;
import com.albertodepaola.logparser.model.Configuration;
import com.albertodepaola.logparser.model.ParserResult;

public class FileExecutorService {

	private static ExecutorService executor;

	/**
	 * Creates a callable that takes the file to parse, creates an ABParser
	 * and returns the result
	 * 
	 * @param fileToParse
	 * @return Callable object to use with executor
	 */
	private static Callable<Tuple<Status, File>> callable(File fileToParse) {
		return () -> {
			Status status = Status.OK;
			try {
				Map<String, String> argumentsMap = new HashMap<>();

				// TODO change to input or binary, else they have to see the same dir structure
				argumentsMap.put("accesslog", fileToParse.getAbsolutePath());
				// TODO solve config file coupling
				Configuration.loadConfigurationFromJsonFile("config.json");

				Parser<ABParserResult> abparser = new ParserFactory<ABParserResult>()
						.createParser(new ABParserFactory(), argumentsMap);

				ParserResult<ABParserResult> parse = abparser.parse();


			} catch (Exception e) {
				e.printStackTrace();
				status = Status.ERROR;
			}

			return new Tuple<Status, File>(status, fileToParse);
		};
	}

	public static Future<Tuple<Status, File>> submitFile(File file) {

		Callable<Tuple<Status, File>> c = callable(file);

		ExecutorService ex = getExecutor();

		Future<Tuple<Status, File>> submit = ex.submit(c);

		return submit;

	}

	private static ExecutorService getExecutor() {
		if (executor == null) {
			executor = Executors.newWorkStealingPool();
		}
		return executor;
	}

}
