package de.amirrocker.functional

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


/**
 * File SubjectAsATopic looks at the Topic: Subject
 * Rx offers a number of different Subjects. Why and what they do is what we want to look at.
 *
 * Subjects have all the operators Observables have. Like Observer it can subscribe to listen for values.
 * And like Observable it cannot be reused once subscribed too. If you think about the nature of Cold Observables
 * it does make sense. Subject passes values through itself.
 * A Subject is a combination of Observer and Observable
 *
 * Flavors:
 * AsyncSubject
 * ReplaySubject
 * BehaviorSubject
 * PublishSubject
 *
 */
fun playWithSubjects() {

    /**
     * the first Type is the PublishSubject.
     * It emits to an observer only those items that were emitted by the observable sources
     * subsequent to the time of the subjects subscription.
     */
    val observable = Observable.interval(100, TimeUnit.MILLISECONDS)

    val subject = PublishSubject.create<Long>()

//    signature params: subscribe(observer:Observer<in Long!>!)
    observable.subscribe(subject)
    observable.subscribe {
        println("observable has received its upstream emission.")
    }

    subject.subscribe(
        {
            println("subject recived observables upstream emission $it")
        }
    )

    runBlocking {
        delay(5000)
    }

    /**
     *
     */
    subject.subscribe(
        {
            println("subject 2 recived observables upstream emission $it") // TODO redo message
        }
    )
    runBlocking {
        delay(5000)
    }
}


/**
 * TODO - copy over the existing subject explanation from other project!
 *      Finish the code and the description
 */
fun playWithAsyncSubject() {

    val observable = Observable.just(1, 2, 3, 4, 5)

    val subject = AsyncSubject.create<Int>()

    observable.subscribe(subject)

    subject.subscribe(
        {
            println("received $it")
        }
    )
}

















