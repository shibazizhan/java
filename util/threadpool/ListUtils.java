package threadpool;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * list相关操作
 *
 * @date 2019/04/18
 */
public class ListUtils {

    public static <T> List<List<T>> divideList(List<T> list, int pageSize) {
        return Lists.partition(list, pageSize);
    }

    /**
     * 给list均匀分组
     * <pre>
     *     12条数据数组分5组后结果：
     *     每组index:
     *     [0,1,10],[2,3,11],[4,5],[6,7],[8,9]
     * </pre>
     *
     * @param list
     * @return
     */
    public static <T> List<List<T>> groupListBalancedly(List<T> list, int maxGroupNum) {
        int total = list.size();
        int maxIndex = total - 1;
        int groupCapacity = total / maxGroupNum;
        if (groupCapacity == 0) {
            groupCapacity = 1;
        }
        List<List<T>> groups = Lists.newArrayListWithCapacity(maxGroupNum);
        for (int i = 0; i < maxGroupNum; i++) {
            int groupStartIndex = i * groupCapacity;
            // 当总数量小于队列长度时
            if (groupStartIndex > maxIndex) {
                break;
            }
            int groupEndIndex = groupStartIndex + groupCapacity;
            groups.add(new ArrayList<>(list.subList(groupStartIndex, groupEndIndex)));
        }
        if (total > maxGroupNum && total % maxGroupNum != 0) {
            // 把没有分到组的散布到现有的组中
            spreadTheRemaining(list.subList(maxGroupNum * groupCapacity, total), groups);
        }
        return groups;
    }

    /**
     * 把未分到组的数据散布到已分的组中（从前往后）
     *
     * @param list
     * @param groups
     * @param <T>
     */
    private static <T> void spreadTheRemaining(List<T> list, List<List<T>> groups) {
        int total = list.size();
        if (total > groups.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < list.size(); i++) {
            groups.get(i).add(list.get(i));
        }
    }
}
