package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 图书馆管理系统主启动类
 * Database Assignment - Library Management System
 */
@SpringBootApplication
@EnableDiscoveryClient
public class DatabaseAssignmentLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseAssignmentLibraryApplication.class, args);
    }

}
