package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.TestTask;

public interface TestTaskRepository extends JpaRepository<TestTask, Integer> {

}
