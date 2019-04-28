package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.TestTask;

import java.util.List;

public interface TestTaskRepository extends JpaRepository<TestTask, Integer> {

    //查询所有启动中的定时任务
    List<TestTask> findByActivedAndStatus(Boolean actived, Boolean status);
}
