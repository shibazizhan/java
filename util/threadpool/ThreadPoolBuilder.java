package threadpool;
import java.lang.IllegalArgumentException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 线程池构建，使用方式见：com.jd.gtr.common.concurrent.ThreadPoolBuilder#main(java.lang.String[])
 *
 * @date 2019/04/18
 */
public class ThreadPoolBuilder<T> {
    /**
     * 核心线程数，选填
     */
    private int threadNum = ThreadPoolProcessor.DEFAULT_MAX_THREAD_NUMBER;
    /**
     * 最长队列，选填
     */
    private int maxQueueNum = ThreadPoolProcessor.DEFAULT_MAX_QUEUE_NUMBER;
    /**
     * 每页执行数量，选填
     */
    private int pageSize = ThreadPoolProcessor.DEFAULT_PAGE_SIZE;
    /**
     * 业务名称，选填
     */
    private String biz;
    /**
     * 是否出错就停止后续队列的执行，选填
     */
    private boolean stopWhenError = false;

    /**
     * 线程池创建：泛型为处理元素的类型，<br>
     * 将创建一个按照分页大小分页、按照队列长度分组的线程池
     * <pre>
     * 必填：
     *    处理的数据list(可分割的list)
     *    线程处理器{@link ThreadProcessor}
     * </pre>
     *
     * <pre>
     * 选填：
     *    核心线程数(默认9)
     *    最长队列(默认2000)
     *    每页执行数量(默认1000)
     *    业务名称(默认空串)
     *    是否出错就停止后续队列的执行(默认false)
     * </pre>
     *
     * @param <T>
     * @return
     */
    public static <T> ThreadPoolBuilder<T> newBuilder() {
        return new ThreadPoolBuilder<T>();
    }

    public ThreadPoolBuilder<T> setThreadNum(int threadNum) {
        if (threadNum < 1) {
            throw new CarGtrException(CodeEnum.INTERNAL_SERVICE_ERRORS, "线程数不能小于1");
        }
        this.threadNum = threadNum;
        return this;
    }

    /**
     * 线程池队列长度, 决定分组数量
     *
     * @param maxQueueNum
     * @return
     */
    public ThreadPoolBuilder<T> setMaxQueueNum(int maxQueueNum) {
        if (maxQueueNum < 1) {
            throw new CarGtrException(CodeEnum.INTERNAL_SERVICE_ERRORS, "最大队列长度不能小于1");
        }
        this.maxQueueNum = maxQueueNum;
        return this;
    }

    /**
     * 数据分页大小
     *
     * @param pageSize
     * @return
     */
    public ThreadPoolBuilder<T> setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new CarGtrException(CodeEnum.INTERNAL_SERVICE_ERRORS, "pageSize不能小于1");
        }
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 执行的业务
     *
     * @param biz
     * @return
     */
    public ThreadPoolBuilder<T> setBiz(String biz) {
        this.biz = biz;
        return this;
    }

    /**
     * 子线程执行异常就停止排队的队列
     *
     * @return
     */
    public ThreadPoolBuilder<T> setStopWhenError() {
        this.stopWhenError = true;
        return this;
    }

    public ThreadPoolProcessor<T> build() {
        if (maxQueueNum < threadNum) {
            throw new IllegalArgumentException("队列数不能小于线程数");
        }
        return new ThreadPoolProcessor<>(threadNum, maxQueueNum, pageSize, biz, stopWhenError);
    }

    public static void main(String[] args) {
        ThreadPoolResult result = ThreadPoolBuilder
                .<Integer>newBuilder()
                .setBiz("test")
                .setPageSize(2)
//                .setStopWhenError()
//                .setThreadNum(5)
//                .setMaxQueueNum(10)
                .build()
                .execute(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)),
                        pages -> pages.forEach(page -> page.forEach(System.out::println)));
        System.out.println(result.isAllThreadsSuccess());
        System.out.println("耗时" + result.getTimeCost());
    }
}
