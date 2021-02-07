package de.amirrocker.functional


import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.flowables.ConnectableFlowable
import io.reactivex.rxjava3.kotlin.toObservable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class Flowables {


    fun playWith() {

//        RxKotlin way to create an observable - TODO read into the github repo
//                                                    https://github.com/ReactiveX/RxKotlin
//        val disposableObservable = listOf("s", "d").toObservable()
//        val disposableFlowable = listOf("s", "d").toFlowable()


//        RxJava - the old fashioned way
//        let's start again - with a ConnectableObservable - a hot observable

        /**
         * Now why and where use Connectable-Flowable and -Observable?
         * Unlike regular observables which emit the source items to each subscriber separately the
         * connectable allows us to collect all interested parties and emit to them
         * once connect is called.
         * Note that subscription 4 will not receive any data since the emissions have already happened.
         * But to show that we are indeed dealing with a hot observable, consider
         * the seconds example.
         */
        val connectableFlowable = listOf("s", "a", "b").toObservable()
            .publish()
        connectableFlowable.subscribe(
            {
                println("subscription 1 : onNext called: $it ")
            },
            {
                println("subscription 1 : onError called: $it ")
            },
            {
                println("subscription 1 : onComplete called")
            }
        )
        connectableFlowable
            .map {
                println("map $it : String to something else")
                it + " an appended string"
            }
            .subscribe(
                {
                    println("Subscription 2 : onNext called on $it")
                }
            )

        connectableFlowable.subscribe(
            {
                println("subscription 3 : $it")
            }
        )

        connectableFlowable.connect()

//        will not receive any emissions since it subscribed after connect call was made.
        connectableFlowable.subscribe(
            {
                println("subscription 4 : $it")
            }
        )

        val intervalEmission = Flowable
            .interval(0, 500, TimeUnit.MILLISECONDS )
            .map {
                " ${((it * 500)/1000).toInt()} seconds have passed."
            }
            .publish()

        intervalEmission.subscribe(
            {
                println("subscription 1 : $it")
            }
        )

        intervalEmission.subscribe({
            println("subscription 2 : $it")
        })

        intervalEmission.connect()

        intervalEmission.subscribe(
            {
                println("subscription 3 : $it")
            }
        )

        runBlocking {
            println("blocking....")
            delay(5000)
        }
    }

    /**
     * multicasting can be visualized like this example.
     * Its very much like before but with a twist.
     * See if you can spot it.
     */
    fun testMulticasting() {

        val connectable = ConnectableFlowable
            .interval(100, TimeUnit.MILLISECONDS)
            .publish()

        connectable.subscribe(
            {
                println("sub 1 $it")
            },
            {
                println("onError 1 $it")
            },
            {
                println("onComplete 1")
            }
        )

        connectable.subscribe(
            {
                println("sub 2 $it")
            },
            {
                println("onError 2 $it")
            },
            {
                println("onComplete 2")
            }
        )

        connectable.subscribe(
            {
                println("sub 3 $it")
            },
            {
                println("onError 3 $it")
            },
            {
                println("onComplete 3")
            }
        )

        connectable.connect()

        runBlocking {
            delay(2000)
        }

        connectable.subscribe(
            {
                println("sub 4 $it")
            },
            {
                println("onError 4 $it")
            },
            {
                println("onComplete 4")
            }
        )

        runBlocking {
            delay(5000)
        }

    }

}