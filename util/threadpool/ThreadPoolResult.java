package threadpool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 执行结果
 *
 * @date 2019/04/17
 */
@Getter
@AllArgsConstructor
public class ThreadPoolResult {
    /**
     * 所有任务是否加载成功
     */
    private boolean loadAllTasksSuccessfully;
    /**
     * 任务加载失败时的异常
     */
    private Throwable exception;
    /**
     * 所有子线程的执行结果
     */
    private List<ThreadResult> threadResults;
    /**
     * 完成后耗时，单位：s，误差500ms内
     */
    private long timeCost;

    /**
     * 是否所有的子线程执行成功
     *
     * @return
     */
    public boolean isAllThreadsSuccess() {
        if (CollectionUtils.isEmpty(threadResults)) {
            return false;
        }
        return threadResults.stream().allMatch(ThreadResult::isSuccess);
    }

    @Getter
    @AllArgsConstructor
    public static class ThreadResult {
        private boolean success;
        private Throwable exception;
    }
}
