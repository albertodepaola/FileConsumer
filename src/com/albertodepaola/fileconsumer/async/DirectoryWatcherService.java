package com.albertodepaola.fileconsumer.async;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.albertodepaola.fileconsumer.model.Status;
import com.albertodepaola.fileconsumer.model.Tuple;

/**
 * Serviço feitor para observar um diretorio por arquivos com determinada
 * extensão, processar eles com o ABParser e mover eles para um diretorio de
 * saida.
 * 
 * @author alberto
 *
 */
public class DirectoryWatcherService {

	private static final String ACCEPTED_EXTENSION_ERROR = ".error.dat";
	private static final String ACCEPTED_EXTENSION_DONE = ".done.dat";
	private static final String ACCEPTED_EXTENSION = ".dat";
	private Path dirIn;
	private Path dirOut;
	private ExecutorService executor;
	private WatchService watcher;

	/**
	 * Construtor que toma o direotorio à verificar e o diretorio de saida
	 * 
	 * @param dirIn
	 *            onde será registrado o watchService
	 * @param dirOut
	 *            onde serão colocados os arquivos de saida
	 */
	public DirectoryWatcherService(Path dirIn, Path dirOut) {
		this.dirIn = dirIn;
		this.dirOut = dirOut;
	}

	/**
	 * Função que inicia o serviço e devolve o controle
	 */
	public void start() {

		// cria um Executor de uma thread, para independizar o serviço de
		// verificação e devolver o controle
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}

		executor.submit(() -> {
			watchDirectory();
		});

	}

	/**
	 * Esta função registra um watchService no dirIn para verificar somente a
	 * criação de arquivos com a extensão ACCEPTED_EXTENSION. Adicionalmente,
	 * verifica a existencia de arquivos no diretorio antes de começar. Cada
	 * arquivo e procesado pelo parser em paralelo, e quando finalizado é movido
	 * para o dirOut, também em paralelo.
	 */
	private void watchDirectory() {
		try {

			// Primeiro verificar os arquivos já existentes no diretorio
			List<Future<Tuple<Status, File>>> results = new ArrayList<>();
			String[] list = dirIn.toFile().list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(ACCEPTED_EXTENSION);
				}
			});
			for (String filename : list) {
				Future<Tuple<Status, File>> existingFile = FileExecutorService
						.submitFile(dirIn.resolve(filename).toFile());

				results.add(existingFile);
			}

			// Processar arquivos existentes
			processResults(results);

			// Criar watcher com variable de tipo instancia para poder para o
			// serviço mais tarde
			watcher = FileSystems.getDefault().newWatchService();
			dirIn.register(watcher, ENTRY_CREATE);

			WatchKey key;

			// enquanto não tem nada, aguarda
			while ((key = watcher.take()) != null) {
				// assim que recupera uma lista de eventos, realiza o reset para
				// poder escutar novos. Caso contrario novos arquivos se perdem
				key.reset();

				for (WatchEvent<?> event : key.pollEvents()) {

					// o .context() contem o nome do arquivo
					if (!event.context().toString().endsWith(ACCEPTED_EXTENSION)) {
						continue;
					}

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;

					// envia o arquivo para ser parseado e guarda um futuro
					Future<Tuple<Status, File>> submitFile = FileExecutorService
							.submitFile(dirIn.resolve(ev.context()).toFile());

					results.add(submitFile);

				}

				// Processar arquivos novos
				processResults(results);

			}

		} catch (IOException x) {
			System.err.println(x);
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}

	/**
	 * Para cada objeto da lista de resultados, verifica o status e move o
	 * arquivo para o diretorio de saida.
	 * 
	 * @param results
	 */
	private void processResults(List<Future<Tuple<Status, File>>> results) {

		// para cada resultado, executa em paralelo, já que a ordem da lista não
		// define a ordem do retorno de future.get()
		results.parallelStream().forEach(future -> {
			try {

				// quando ainda não finalizou o parse, fica aguardando aqui
				Tuple<Status, File> result = future.get();

				
				if (Status.OK.equals(result.x)) {
					Files.move(result.y.toPath(), dirOut.resolve(result.y.toPath().getFileName().toString()
							.replace(ACCEPTED_EXTENSION, ACCEPTED_EXTENSION_DONE)), StandardCopyOption.REPLACE_EXISTING);
				} else {
					Files.move(result.y.toPath(), dirOut.resolve(result.y.toPath().getFileName().toString()
							.replace(ACCEPTED_EXTENSION, ACCEPTED_EXTENSION_ERROR)), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
	}

	/**
	 * Permite parar o serviço de verificação
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void stop() throws InterruptedException, IOException {
		if (watcher != null)
			watcher.close();

		executor.shutdown();
	}

}
