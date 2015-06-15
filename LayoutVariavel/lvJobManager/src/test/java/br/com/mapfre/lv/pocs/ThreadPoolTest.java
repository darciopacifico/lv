package br.com.mapfre.lv.pocs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPoolTest {

	static class DummyRunnable implements Runnable{
		private int threadNum;
		
		public DummyRunnable(int threadNum){
			this.threadNum = threadNum;
		}
		
		@Override
		public void run() {
			int sleepTimeout = 3000;
			
			//System.out.println("executando thread "+this.threadNum+" e domindo "+sleepTimeout+"ms!");
			try {
				Thread.sleep(sleepTimeout);
			} catch (InterruptedException e) {
				throw new RuntimeException("Erro ao tentar parar a thread",e);
			}
		}
	}

	
	public static void main(String[] args) {
		
		//ExecutorService executorService = Executors.newFixedThreadPool(3);
		//ExecutorService executorService = Executors.
		int tamanhoFilaThreads = 10;
		int threadsExecutedAtSameTime = 1;
		int threadsNoPool = 3;
		int timeoutThreadPool = 2;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(tamanhoFilaThreads);
		RejectedExecutionHandler rejectHandler	= new ThreadPoolExecutor.CallerRunsPolicy()	;
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(threadsExecutedAtSameTime, threadsNoPool, timeoutThreadPool, unit, workQueue,rejectHandler);
		
		
		for(int i=0; i<100; i++){
			//System.out.println("thread "+i+" submetida");
			
			try{
				executor.execute(new DummyRunnable(i));
			}catch (RejectedExecutionException e) {
				System.out.println("O pool de threads rejeitou o enfileiramento de novas threads");
				System.out.println("As threads atualmente enfileiradas serão executadas e o pool será encerrado!");
				break;
				// TODO: handle exception
			}
		}
		
		executor.shutdown();
	}

}
