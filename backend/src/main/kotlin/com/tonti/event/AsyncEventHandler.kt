package com.tonti.event

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Composed annotation for async event handlers that run in their own transaction.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Async
@EventListener
@Transactional(propagation = Propagation.REQUIRES_NEW)
annotation class AsyncEventHandler
