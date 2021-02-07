package de.amirrocker.functional

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.BiPredicate
import io.reactivex.rxjava3.kotlin.toObservable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit
import kotlin.math.nextTowards
import kotlin.math.nextUp
import kotlin.math.roundToInt
import kotlin.random.Random


/**
 * Operators for
 * - filtering and suppressing
 * - transforming
 * - reducing
 * - collecting
 * - error handling
 *
 * Taking a closer look:
 * filtering/suppressing:
 * - debounce
 * - filter
 * - distinct
 * - skip, skipLast, skipFirst, skipUntil, skipWhile
 * - elementAt
 * - first, last
 * - take, takeFirst, takeLast, takeUntil, takeWhile
 *
 *
 */
fun runOperators() {

    fun filteringAndSuppressing() {
        /**
         * debounce
         */
        simulateKeystrokes()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribe {
                println("received: $it")
            }
    }
    /* TODO >>> improve on the timing and behavior! refactor out the primitives! */
    filteringAndSuppressing()

    /**
     * distinct operator
     * remembers each emission made and filters out doubles.
     */
    fun distinctOperators() {

        listOf(1, 2, 3, 2, 5, 6, 3, 2, 5, 5, 5, 6, 7, 8, 3, 2, 4, 2, 3, 4, 5, 4, 6, 6, 7).toObservable()
            .distinct()
            .subscribe({
                println("onNext: $it")
            }, {
                error(it)
            }, {
                println("onComplete.")
            })

    }

    /**
     * distinctUntilChanged operator
     * remembers each emission made and filters out doubles.
     * using a BiPredicate we can control how the operator behaves
     * TODO show a sensible example
     */
    fun distinctUntilChangedOperators() {

        listOf(1, 2, 3, 2, 5, 6, 3, 2, 5, 5, 5, 6, 7, 8, 3, 2, 4, 2, 3, 4, 5, 4, 6, 6, 7).toObservable()
            .distinctUntilChanged()
            .subscribe({
                println("onNext: $it")
            }, {
                error(it)
            }, {
                println("onComplete.")
            })

        listOf(1, 2, 3, 2, 5, 6, 3, 2, 5, 5, 5, 6, 7, 8, 3, 2, 4, 2, 3, 4, 5, 4, 6, 6, 7).toObservable()
            .distinctUntilChanged(BiPredicate { t1, t2 ->
                t1 < t2
            })
            .subscribe({
                println("onNext: $it")
            }, {
                error(it)
            }, {
                println("onComplete.")
            })

    }



}

val keystrokes = arrayOf("K", "Ke", "Key", "Keys", "Keyst", "Keystr", "Keystro", "Keystrok", "Keystroke",
    "Keystroke r", "Keystroke re", "Keystroke rec", "Keystroke rece", "Keystroke recei", "Keystroke receiv", "Keystroke receive", "Keystroke received"
)

// Note: this function is an expression and not a regular statement.
// TODO talk about expressions
fun simulateKeystrokes() : Observable<String> =
    Observable.create {
        for( index in 1..16) {
            it.onNext(keystrokes[index])

            val random = Math.random().nextTowards(1.0).roundToInt()
            val randomValue = Math.random().nextTowards(1.0)
//            println("random: $random with value: $randomValue")
            if(random==1) {
                runBlocking { delay((randomValue * 1000).toLong()) }
            }
        }
    }




