package de.amirrocker.functional

import io.reactivex.rxjava3.core.CompletableSource
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * any application most likely has a number of different datasources.
 *
 * - startWith
 * - zip
 * - merge
 * - concat
 * - combineLatest
 *
 */
fun combineData() {

    /**
     * startWith
     */
    fun useStartWithOperator() {
        val flowable = Flowable.range(5, 10)
        flowable
            .startWith(CompletableSource {
                listOf(1, 2, 3, 4)
            })
            .observeOn(Schedulers.computation())
            .subscribe {
                println("subscribe onNext: $it")
            }

        Observable.range(5, 10)
            .startWith(Observable.just(1, 2, 3, 4))
            .observeOn(Schedulers.computation())
            .subscribe({
                println("onNext: $it")
            }, {
                error(it)
            }, {
                println("onComplete")
            })

    }

    // this can be demonstrated rather easy ( for show only !)
    // in real prod. code we would have error and retry handling, logging,
    // and async behavior.

    /**
     * the zip operator is a mighty tool.
     * it allows to combine an arbitrary number of producers
     * into a single stream of data. Imagine receiving data
     * from a number of sources and wanting to combine them.
     * That's the Zip operator.
     */
    fun funWithTheZipOperator() {
        val resultRepoOne = Observable.range(1, 20)
        val resultRepoTwo = Observable.range(11, 20)
        // we define how we zip
        Observable.zip(resultRepoOne, resultRepoTwo, BiFunction { t1, t2 ->
            t1 + t2
        })
        .subscribe {
            println("received zipped value: $it")
        }

    }




}




