package com.lunar.stripelunar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for enabling scheduled tasks in the application
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // This class enables scheduling with the @EnableScheduling annotation
    // No additional configuration is needed for basic scheduling
}
