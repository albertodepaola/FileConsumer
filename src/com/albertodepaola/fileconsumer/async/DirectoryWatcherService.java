package com.albertodepaola.fileconsumer.async;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.albertodepaola.fileconsumer.model.Tuple;

public class DirectoryWatcherService {

	private Path dirIn;
	private Path dirOut;
	private ExecutorService executor;
	private WatchService watcher;

	public DirectoryWatcherService(Path dirIn, Path dirOut) {
		this.dirIn = dirIn;
		this.dirOut = dirOut;
	}

	public void start() {

		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}

		executor.submit(() -> {
			watchDirectory();
		});

	}

	private void watchDirectory() {
		try {
			watcher = FileSystems.getDefault().newWatchService();

			List<Future<Tuple<String, File>>> results = new ArrayList<>();
			String[] list = dirIn.toFile().list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".dat");
				}
			});
			for (String filename : list) {
				System.out.println("Filename: " + filename);
				Future<Tuple<String, File>> existingFile = FileExecutorService
						.submitFile(dirIn.resolve(filename).toFile());
				
				results.add(existingFile);
			}

			processResults(results);

			dirIn.register(watcher, ENTRY_CREATE);

			WatchKey key;

			while ((key = watcher.take()) != null) {
				key.reset();

				for (WatchEvent<?> event : key.pollEvents()) {
					System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");

					if (!event.context().toString().endsWith(".dat")) {
						continue;
					}

					// TODO send file to executorservice to process async
					WatchEvent<Path> ev = (WatchEvent<Path>) event;

					Future<Tuple<String, File>> submitFile = FileExecutorService
							.submitFile(dirIn.resolve(ev.context()).toFile());

					results.add(submitFile);

				}

				processResults(results);

			}

		} catch (IOException x) {
			System.err.println(x);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	private void processResults(List<Future<Tuple<String, File>>> results) {
		
		results.parallelStream().forEach(future -> {
			try {
				
				Tuple<String, File> result = future.get();
				
				if ("OK".equals(result.x)) {
					long startTime = System.currentTimeMillis();
					Path move = Files.move(result.y.toPath(),
							dirOut.resolve(result.y.toPath().getFileName().toString().replace(".dat", ".done.dat")),
							REPLACE_EXISTING);
					long diff = System.currentTimeMillis() - startTime;
					System.out.println("Move time: " + diff + " - " + (diff / 1000));
				} else {
					
					Path move = Files.move(result.y.toPath(),
							dirOut.resolve(result.y.toPath().getFileName().toString().replace(".dat", ".error.dat")),
							REPLACE_EXISTING);
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
	}

	public void stop() throws InterruptedException, IOException {
		if (watcher != null)
			watcher.close();

		executor.shutdown();
	}

}
