package com.tonti.config

import mu.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Executor

private val logger = KotlinLogging.logger {}

@Configuration
class AsyncConfig : AsyncConfigurer {

    @Bean("eventExecutor")
    override fun getAsyncExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 10
            queueCapacity = 100
            setThreadNamePrefix("tonti-event-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)
            initialize()
        }
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { ex: Throwable, method: Method, vararg params: Any ->
            logger.error(ex) { "Async error in ${method.name} with params ${params.contentToString()}" }
        }
    }
}
