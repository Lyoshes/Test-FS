import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Should be improved to reduce calculation time.
 *
 * Change it or create new one. (max threads count is com.fitechsource.test.TestConsts#MAX_THREADS)
 */
public class TestR {

	/**
	 * MyLogger
	 * */
	private static class MyLogger 
	{
		public static void error(String s) 
		{
			System.err.println(s);
		}

		public static void error(String s, Object...args) 
		{
			System.err.println(String.format(s, args));
		}

		public static void info(String s, Object...args) 
		{
			System.out.println(String.format(s, args));
		}

		public static void info(String s) 
		{
			System.out.println(s);
		}
	}

	/**
	 * MyTaskFactory
	 * */
	private static class MyTaskFactory 
	{
		private ConcurrentSkipListSet<Double> res;

		public MyTaskFactory(ConcurrentSkipListSet<Double> res) 
		{
			this.res = res;
		}

		public static MyTaskFactory newMyTaskFactory(ConcurrentSkipListSet<Double> res)
		{
			if (res == null)
				throw new IllegalArgumentException("Can not be null");

			return new MyTaskFactory(res);
		}

		public MyTask createTask(int i) throws TestException
		{
			return new MyTask(i, res);
		}

	}


	/**
	 * MyTask
	 * */
	private static class MyTask implements Runnable 
	{
		private int i;
		private ConcurrentSkipListSet<Double> res;

		public MyTask(int i, ConcurrentSkipListSet<Double> res) 
		{
			this.i = i;
			this.res = res;
		}

		@Override
		public void run()
		{			
			try {
				//FIXME: For test
				if (i == 2)
					throw new TestException("Test exception.");

				Set<Double> synchSet = TestCalc.calculate(i);

				Iterator<Double> iterator = synchSet.iterator();
				while (iterator.hasNext()) 
				{
					res.add(iterator.next());
				}
				//res.addAll(TestCalc.calculate(i));
			} catch (TestException e) {
				res.clear();
				throw new RuntimeException(e);
			}
		}
	}


	/**
	 * MyThreadPoolExecutor
	 * */
	public static class MyThreadPoolExecutor extends ThreadPoolExecutor {

		public static final long AWAIT = 60;

		public MyThreadPoolExecutor(int maxThreads) {
			super(maxThreads, maxThreads, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>());
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) 
		{
			super.afterExecute(r, t);

			if (t != null)
				t.printStackTrace();

			if (t == null && r instanceof Future<?>) 
			{
				try {
					Future<?> future = (Future<?>) r;
					if (future.isDone()) {
						future.get();
					}
				} catch (CancellationException ce) {
					t = ce;
				} catch (ExecutionException ee) {
					t = ee.getCause();
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} 
			}
			
			if (t != null) 
			{
				MyLogger.error("Error: %s, Time: %s, Thread: [%s]", t.getMessage(), new Date() , Thread.currentThread().getName());
				shutdownNow();
			}
		}
	}

	/**
	 * main
	 * */
	public static void main(String[] args) throws TestException 
	{
		long startTime = System.currentTimeMillis();

//		 Set<Double> set = new HashSet<Double>();
//		 Set<Double> res = Collections.synchronizedSet(set);
		 
		ConcurrentSkipListSet<Double> res = new ConcurrentSkipListSet<Double>();

		ExecutorService serviceExecutor = new MyThreadPoolExecutor(TestConsts.MAX_THREADS);
		MyTaskFactory myThreadFactory = MyTaskFactory.newMyTaskFactory(res);

		for(int i = 0; i < TestConsts.N; i++)
		{			
			serviceExecutor.submit(myThreadFactory.createTask(i));
		}

		try {
			MyLogger.info("shutdown executor");
			serviceExecutor.shutdown();
			serviceExecutor.awaitTermination(MyThreadPoolExecutor.AWAIT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			MyLogger.error("threads interrupted");
		}
		finally {
			if (!serviceExecutor.isTerminated()) {
				MyLogger.error("cancel non-finished threads");
			}
			serviceExecutor.shutdownNow();
			MyLogger.info("shutdown finished");
		}

		MyLogger.info("time: %s ms, size: %s, result: %s", System.currentTimeMillis() - startTime, res.size(), res);
	}
}
