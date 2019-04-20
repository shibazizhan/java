package threadpool;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jd.gtr.common.utils.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池相关util，使用方式见：com.jd.gtr.common.concurrent.ThreadPoolBuilder#main(java.lang.String[])
 *
 * @date 2019/04/17
 */
@Slf4j
public class ThreadPoolProcessor<T> {
    // -------以下是默认参数-------
    /**
     * IO密集型：2*cpu数量 + 1
     */
    static final int DEFAULT_MAX_THREAD_NUMBER = 9;
    /**
     * 默认分页大小
     */
    static final int DEFAULT_PAGE_SIZE = 1000;
    /**
     * 最大2000个执行队列
     */
    static final int DEFAULT_MAX_QUEUE_NUMBER = 2000;
    // -------以下是可选设置参数-------
    /**
     * 核心线程数
     */
    private int maxThreadNum = DEFAULT_MAX_THREAD_NUMBER;
    /**
     * 最长队列
     */
    private int maxQueueNum = DEFAULT_MAX_QUEUE_NUMBER;
    /**
     * 每页执行数量
     */
    private int pageSize = DEFAULT_PAGE_SIZE;
    /**
     * 业务名称
     */
    private String biz;
    /**
     * 是否出错就停止后续队列的执行
     */
    private boolean stopWhenError = false;
    // -------以下是线程池执行所用参数-------
    /**
     * 是否执行过程中出错
     */
    private volatile boolean hasError = false;
    /**
     * 子线程执行结果
     */
    private List<ThreadPoolResult.ThreadResult> threadResults;
    /**
     * 已执行任务数量
     */
    private AtomicInteger executedTaskNums = new AtomicInteger();

    /**
     * 建议用{@link ThreadPoolBuilder}进行创建
     *
     * @param maxThreadNum
     * @param maxQueueNum
     * @param pageSize
     * @param biz
     * @param stopWhenError
     */
    public ThreadPoolProcessor(int maxThreadNum, int maxQueueNum, int pageSize, String biz, boolean stopWhenError) {
        this.maxThreadNum = maxThreadNum;
        this.maxQueueNum = maxQueueNum;
        this.pageSize = pageSize;
        this.biz = biz == null ? "" : biz;
        this.stopWhenError = stopWhenError;
    }

    public ThreadPoolProcessor(String biz) {
        this.biz = biz == null ? "" : biz;
    }

    /**
     * 多线程处理数据
     *
     * @param data            数据
     * @param threadProcessor 子线程处理逻辑
     * @return
     */
    public ThreadPoolResult execute(List<T> data, ThreadProcessor<T> threadProcessor) {
        log.error("{}线程池处理开始...", biz);
        Instant start = Instant.now();
        try {
            process(data, threadProcessor);
            long timeCost = Duration.between(start, Instant.now()).getSeconds();
            log.error("{}线程池处理结束...耗时：{}", biz, timeCost);
            return new ThreadPoolResult(true, null, this.threadResults, timeCost);
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("{}线程池开启异常：", biz, e);
            return new ThreadPoolResult(false, e, this.threadResults, Duration.between(start, Instant.now()).getSeconds());
        }
    }

    private void process(List<T> data, ThreadProcessor<T> threadProcessor) {
        List<List<T>> pageList = ListUtils.divideList(data, pageSize);
        // 按照最大队列长度分配分页数据，防止超队列
        List<List<List<T>>> slices = ListUtils.groupListBalancedly(pageList, maxQueueNum);
        int actualQueueSize = slices.size();
        if (actualQueueSize == 0) {
            log.info("{}待处理的数据为空,线程池结束", biz);
            return;
        }
        log.info("{}线程池实际队列长度: {}", biz, actualQueueSize);
        this.threadResults = Lists.newArrayListWithCapacity(actualQueueSize);
        ThreadPoolExecutor threadPoolExecutor = initThreadPool(actualQueueSize);
        try {
            for (List<List<T>> slice : slices) {
                // 遇错即止可能使线程池提前结束
                if (!threadPoolExecutor.isTerminated()) {
                    threadPoolExecutor.execute(() -> {
                        try {
                            if (hasError && stopWhenError) {
                                threadPoolExecutor.shutdownNow();
                                return;
                            }
                            threadProcessor.execute(slice);
                            executedTaskNums.incrementAndGet();
                            this.threadResults.add(new ThreadPoolResult.ThreadResult(true, null));
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("{}子线程执行异常: ", biz, e);
                            hasError = true;
                            this.threadResults.add(new ThreadPoolResult.ThreadResult(false, e));
                        }
                    });
                }
            }
        } finally {
            threadPoolExecutor.shutdown();
        }
        while (!threadPoolExecutor.isTerminated()) {
            int executed = executedTaskNums.get();
            log.info("{}当前已执行任务数量：{}, 总数量：{}, 进度：{}%", biz, executed, actualQueueSize, NumberFormat.getPercentInstance().format((double) executed / actualQueueSize));
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                log.info("{}任务执行状况睡眠异常: ", biz);
            }
        }
    }

    private ThreadPoolExecutor initThreadPool(int queueNum) {
        String threadNameFormat = this.biz + "-thread-pool-%d";
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build();
        BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(queueNum);
        RejectedExecutionHandler rejectedExecutionHandler = (r, e) -> log.error("{} BlockingQueue is full!", biz);
        return new ThreadPoolExecutor(this.maxThreadNum, queueNum < maxThreadNum ? maxThreadNum : queueNum, 60L,
                TimeUnit.SECONDS, taskQueue, namedThreadFactory, rejectedExecutionHandler);
    }
}
