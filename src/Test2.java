import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Should be improved to reduce calculation time.
 *
 * Change it or create new one. (max threads count is com.fitechsource.test.TestConsts#MAX_THREADS)
 */
public class Test2 {

	private static int i;

	public static void main(String[] args) throws TestException {

		long t1 = System.currentTimeMillis();
		ConcurrentSkipListSet<Double> res = new ConcurrentSkipListSet<>();
		
		//volatile Set<Double> res = new HashSet<>();
		ExecutorService serviceExecutor = Executors.newFixedThreadPool(TestConsts.MAX_THREADS);
		
       // RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
        //Get the ThreadFactory implementation to use
      //  ThreadFactory threadFactory = Executors.defaultThreadFactory();
		//BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(TestConsts.N);
		//ExecutorService serviceExecutor = new ThreadPoolExecutor(TestConsts.MAX_THREADS, TestConsts.MAX_THREADS, 10, TimeUnit.SECONDS, queue,  threadFactory, rejectionHandler);
		
		//        for (int i = 0; i < TestConsts.N; i++) {
		//            res.addAll(TestCalc.calculate(i));
		//        }
		//
		
		for(i = 0; i < TestConsts.N; i++)
		{
			serviceExecutor.execute(new Runnable() {
				public void run() {
					try {
						res.addAll(TestCalc.calculate(i));
					} catch (TestException e) {
						e.printStackTrace();
					}
					System.out.println(i);
				}
			});
		}

		serviceExecutor.shutdown();
//		while (!serviceExecutor.isTerminated()){
//		}
//		
		try {
			serviceExecutor.awaitTermination(120, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(String.format("Результат: {%s}",res));
		System.out.println(String.format("Время работы: {%s мс}",System.currentTimeMillis()-t1));       
		System.out.println(String.format("Отклонено: {%s}", serviceExecutor.shutdownNow().size()));

	}
}
