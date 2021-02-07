package de.amirrocker.functional

import io.reactivex.rxjava3.kotlin.toFlowable
import io.reactivex.rxjava3.processors.PublishProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


/**
 * Processors are subjects for Flowables.
 * As in Subjects there are a number of processors out of the box to use:
 * - PublishProcessor
 * - AsyncProcessor
 * - RelayProcessor
 * - BehaviorProcessor
 *
 * Processors and Subjects can subscribe to emissions and publish to subscribers.
 * They are the best of both worlds.
 *
 */
fun playWithProcessors() {

    val flowable = listOf("a1", "a2", "a3").toFlowable()
    val processor = PublishProcessor.create<String>()

    processor.subscribe {
        println("subscription1 received $it")
        runBlocking { delay(1000) }
        println("sunscription 1 delay")
    }

    processor.subscribe {
        println("subscription 2 received $it")
    }

    flowable.subscribe(processor)

}