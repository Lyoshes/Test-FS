import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Should be improved to reduce calculation time.
 *
 * Change it or create new one. (max threads count is com.fitechsource.test.TestConsts#MAX_THREADS)
 */
public class Test3 {
	
	volatile static int countReady = 0;
	
    public static void main(String[] args) throws TestException, InterruptedException {
    	ConcurrentSkipListSet<Double> res = new ConcurrentSkipListSet<Double>();
    	
    	Object lock = new Object();
    	
    	
    	
    	class TestTask implements Runnable{
    		
    		private final int num;
    		
    		public TestTask(int num) {
    			this.num = num;
    		}

    		@Override
    		public void run() {
    			try {
    				synchronized (lock) {
    					res.addAll(TestCalc.calculate(num));
    					countReady++;
    					lock.notify();
    				}
    			} catch (TestException e) {
    				e.printStackTrace();
    			}
    		}
    		
    	}
    	
    	Thread threadPrinter = new Thread(new Runnable() {

    		@Override
    		public void run() {
    			synchronized (lock) {

    				while(countReady != TestConsts.N) {
    					try {
    						lock.wait();
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
    				}

    				System.out.println(res);

    			}
    		}
    	});

        ExecutorService service = Executors.newFixedThreadPool(TestConsts.MAX_THREADS);
        
        for (int i = 0; i < TestConsts.N; i++) {
        	service.execute(new TestTask(i));
        }
        
        threadPrinter.start();
        
        
    }
}