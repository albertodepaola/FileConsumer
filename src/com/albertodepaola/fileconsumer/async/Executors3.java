package com.albertodepaola.fileconsumer.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Benjamin Winterberg
 */
public class Executors3 {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// test1();
		// test2();
		// test3();

//		 test4();
		// test5();
		test6();
	}

	private static void test6() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newWorkStealingPool();

		List<Callable<String>> callables = Arrays.asList(callable("task1", 2), callable("task2", 1),
				callable("task3", 3));

		List<Future<String>> result = new ArrayList<>();
		for (Callable<String> callable : callables) {
			Future<String> submit = executor.submit(callable);
			result.add(submit);
		}
		
		
		result.parallelStream().map(future -> {
			try {
				String string = future.get();
				System.out.println(string);
				return string;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}).forEach(System.out::println);
		/*
		for (Future<String> future : result) {
			
			System.out.println(future.get());
		}
		*/
	}

	private static void test5() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newWorkStealingPool();

		List<Callable<String>> callables = Arrays.asList(callable("task1", 2), callable("task2", 1),
				callable("task3", 3));

		String result = executor.invokeAny(callables);
		System.out.println(result);

		executor.shutdown();
	}

	private static Callable<String> callable(String result, long sleepSeconds) {
		return () -> {
			TimeUnit.SECONDS.sleep(sleepSeconds);
			return result;
		};
	}

	private static void test4() throws InterruptedException {
		ExecutorService executor = Executors.newWorkStealingPool();

		List<Callable<String>> callables = Arrays.asList(callable("task1", 2), callable("task2", 1),
				callable("task3", 4));

		executor.invokeAll(callables).parallelStream().map(future -> {
			try {
				String string = future.get();
				System.out.println(string);
				return string;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}).forEach(System.out::println);

		executor.shutdown();
	}

	private static void test3() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			try {
				TimeUnit.SECONDS.sleep(2);
				System.out.println("Scheduling: " + System.nanoTime());
			} catch (InterruptedException e) {
				System.err.println("task interrupted");
			}
		};

		executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
	}

	private static void test2() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable task = () -> System.out.println("Scheduling: " + System.nanoTime());
		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
	}

	private static void test1() throws InterruptedException {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> System.out.println("Scheduling: " + System.nanoTime());
		int delay = 3;
		ScheduledFuture<?> future = executor.schedule(task, delay, TimeUnit.SECONDS);

		TimeUnit.MILLISECONDS.sleep(1337);

		long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
		System.out.printf("Remaining Delay: %sms\n", remainingDelay);
	}

}