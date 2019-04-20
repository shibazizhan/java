
#### 功能简介
- 基于jdk1.8；
- 固定线程、限制最大队列长度的自定义线程池；
- 定制线程池加载任务、子线程各种参数，如分页大小、是否子线程出错就停止；
- 返回线程池分解、加载任务结果、子线程处理结果以及耗时（估量）；
- 根据分页大小分页得到分页集，根据队列大小分配分页集得到线程处理的数据；
- 监控线程池执行状况（500ms刷新一次）
#### 示例
```
ThreadPoolProcessor<Integer> threadPoolProcessor = ThreadPoolBuilder
                .<Integer>newBuilder()
                .setBiz("test")
                .setPageSize(2)
                .setStopWhenError()
                .setThreadNum(5)
                .setMaxQueueNum(10)
                .build();
ThreadPoolResult result = threadPoolProcessor
        .execute(Arrays.asList(1, 2, 3, 4, 5),
                pages -> pages.forEach(page -> page.forEach(System.out::println)));
System.out.println(result.isAllThreadsSuccess());
System.out.println("耗时" + result.getTimeCost());
```
#### 注
依赖的ListUtils也附带过来了，就不另放到其他包中了
