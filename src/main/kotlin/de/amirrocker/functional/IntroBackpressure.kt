package de.amirrocker.functional

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Flowables
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit


/**
 * Backpressure - what is it?
 * 
 * Instead of Observer, used by Observable, Flowables use Subscribers which are backpressure compatible.
 * Subscriber supports extra operations and backpressure. E.g. it can convey how many items it can receive
 * as message to upstream.
 * TODO describe backpressure in short and link to sources.
 */


/**
 * a very simple Domain class to play with...
 */
data class DomainEntity(
    val id: Int
) {
    init {
        println("created: $this with id: $id")
    }
}


class IntroBackpressure {

    fun simpleBackpressureShowcase() {
        Flowable.range(0, 10)
            .map { 
                DomainEntity(it)
            }
            .observeOn(Schedulers.computation())
            .subscribe(object:Subscriber<DomainEntity> {
                override fun onSubscribe(subscription: Subscription?) {
                    println("onSubscribe called s: $subscription")
                    subscription?.let {
                        it.request(Long.MAX_VALUE) // Note: amount of objects capable to handle
                    }
                }

                override fun onNext(t: DomainEntity?) {

                    runBlocking { delay(50) }
                    println("onNext called t: $t : DomainEntity")

                }

                override fun onError(t: Throwable?) {
                    println("")
                }

                override fun onComplete() {
                    println("onComplete called")
                }
            })
            runBlocking { delay(7000) }
    }

    fun moreComplexBackpressure() {
        Flowable.range(0, 15)
            .map {
                DomainEntity(it)
            }
            .observeOn(Schedulers.computation())
            .subscribe(object:Subscriber<DomainEntity>{
                lateinit var subscription: Subscription
                override fun onSubscribe(s: Subscription?) {
                    s?.let {
                        this.subscription = it
                        this.subscription.request(5)
                    }

                }

                override fun onNext(t: DomainEntity?) {
                    runBlocking { delay(50) }
                    t?.let {
                        println("Subscriber received $t")
                        if(it.id==4) {
                            println("Requesting two more @it: $it")
                            subscription.request(2)
                        }
                    }

                }

                override fun onError(t: Throwable?) {
                    println("onError called: $t")
                }

                override fun onComplete() {
                    println("Done!")
                }
            })
        runBlocking { delay(15000) }
    }


    /**
     * A short look back at creation of Observables and Flowables.
     * Lets create a an Observable completely from scratch without
     * using any factory function other than create.
     */
    fun createObservableFromScratch() {

        val observer : Observer<Int> = object : Observer<Int> {
            override fun onSubscribe(d: Disposable?) {
                println("onSubscribe implemented")
            }

            override fun onNext(t: Int?) {
                println("onNext $t")
            }

            override fun onError(e: Throwable?) {
                println("onError $e")
            }

            override fun onComplete() {
                println("onComplete called ....")
            }
        }
        val observable = Observable.create<Int> {
            for( index in 1..15) {
                it.onNext(index)
            }
            it.onComplete()
        }
        observable.subscribe(observer)
    }

    /**
     * A Flowable from scratch
     */
    fun createFlowableFromScratch() {

        val subscriber = object : Subscriber<Int> {
            override fun onSubscribe(s: Subscription?) {
                println("onSubscribe $s")
            }

            override fun onNext(t: Int?) {
                println("onNext $t")
            }

            override fun onError(t: Throwable?) {
                println("onError $t")
            }

            override fun onComplete() {
                println("onComplete Done!")
            }
        }
        val flowable = Flowable.create<Int>({
            for (index in 1..15) {
                it.onNext(index)
            }
            it.onComplete()
        }, BackpressureStrategy.BUFFER)

        val source = flowable
            .observeOn(Schedulers.computation())
            .subscribe(subscriber)

        runBlocking { delay(16000) }

//        TODO check!creating Observables/Flowables from scratch is not as convenient as when using factory methods.
//        val disposable = flowable.subscribe()
    }

    /**
     * how can we utilize backpressure?
     * What backpressure implementations are there?
     * BackpressureStrategy.BUFFER - keeps an unbounded buffer! may lead to memory errors
     * BackpressureStrategy.LATEST - keeps the latest and drops all while downstream is busy
     * BackpressureStrategy.MISSING
     * BackpressureStrategy.DROP
     * BackpressureStrategy.ERROR
     * we can play with the different strategies.
     */
    fun observableToFlowable() {

        val source = Observable.range(1, 1000)
        val disposable = source.toFlowable(BackpressureStrategy.BUFFER) //
            .map {
                DomainEntity(it)
            }
            .observeOn(Schedulers.computation())
            .subscribe {
                println("subscribe it: $it")
                runBlocking { delay(1000)}
            }
        runBlocking { delay(100000) }
        disposable.dispose()
    }


    /**
     * using the BackpressureStrategy.BUFFER option
     */
    fun handleBackpressureMissingOptions() {

        /**
         * a default implementation of onBackpressure()
         */
        fun handleOnBackpressureBuffer() =
            Observable.range(1,1000)
                .toFlowable(BackpressureStrategy.MISSING)
                .onBackpressureBuffer() // a simple unconfigured default option

        /**
         * lets us set capacity, delayError and an overflow strategy
         * Note the naming of the construct - if looked at in detail
         * this backpressure functionality is implemented as a Strategy.
         */
        fun handleOnBackpressureBufferConfigured() =
            Observable.range(1,1000)
                .toFlowable(BackpressureStrategy.MISSING)
                .onBackpressureBuffer(10)

        val source = handleOnBackpressureBuffer()
            .map {
                DomainEntity(it)
            }
//            .filter {
//                it.id!!%2==0
//            }
            .observeOn(Schedulers.computation())
            .subscribe {
                println("subscribe $it")
                runBlocking { delay(1000) }
            }

        runBlocking { delay(600000) }

        source.dispose()

    }


    /**
     *
     * Buffer
     * Window
     * Throttle
     *
     * Unlike the onBackPressureBuffer() operator, Buffer batches the emissions and emit them as list or
     * other collection types.
     *
     */
    fun bufferThrottleAndWindowOperators() {

        val flowable = Flowable
            .range(1, 111)

        flowable.buffer(10, 15)
            .subscribe {
                println("onNext1 : $it")
            }

        flowable.buffer(15, 7)
            .subscribe {
                println("onNext2 : $it")
            }


        flowable
            .buffer(2, TimeUnit.SECONDS)
            .subscribe {
                println("onNext 3 with batch of 2 sec. of emissions: $it")
            }

    }

    /**
     * The window operator is used to buffer a number of emissions inside a Flowable
     * and passing it downstream.
     */
    fun windowOperator() {
        val flowableWithWindow = Flowable.range(0,111)
        flowableWithWindow
            .window(10) // buffers 10 emissions inside a Flowable and passes it downstream
            .subscribe {
                bufferedFlowableFromWindow -> bufferedFlowableFromWindow.subscribe {
                    print("it, ")
                }
                println()

            }
    }


}



















































