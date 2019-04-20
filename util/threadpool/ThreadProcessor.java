package threadpool;

import java.util.List;

/**
 * 线程处理
 *
 * @date 2019/04/17
 */
public interface ThreadProcessor<T> {
    /**
     * 子线程处理逻辑
     *
     * @param pages 分页组，根据队列长度分到的组，组中有分页list
     */
    void execute(List<List<T>> pages);
}
