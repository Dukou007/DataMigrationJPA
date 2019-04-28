package com.jettech.service;

import com.jettech.entity.TestTask;

import java.util.List;

public interface ITestTaskService extends IService<TestTask, Integer> {

    List<TestTask> selectByActivedAndStatus(Boolean actived,Boolean status);
}
