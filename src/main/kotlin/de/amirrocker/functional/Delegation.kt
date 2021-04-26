package de.amirrocker.functional

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * there are a number of different types of delegation in kotlin:
 * - Class Delegation
 * - Property Delegation
 *
 * Starting with Property delegation:
 * - Delegate.NotNull or lateinit
 * - Delegate.Observable -> a basic property changed listener mechanism
 * - Delegate.Vetoable -> a 'filter' like Predicate that must be fullfilled to execute
 * - Delegate.lazy -> by lazy is imho the most prominent since used the most - lazily construct instances
 *
 * I wont look at lazy and also lateinit / NotNull should self explanatory.
 * Instead I rather look at the exots.
 *
 */
class Delegation {

    var observableString: String by Delegates.observable("<SomeInitialValue>") { property: KProperty<*>, oldValue: String, newValue: String ->
        println("ObservableProperty: ${property.name} with values, oldValue: $oldValue and newValue: $newValue")
    }

    /* a simple first example to warm up... */
    fun testDelegateObservable() {
        observableString = "First value"
        observableString = "Second, changed value"
    }

    /**
     * Why should I use this?
     * First, it makes for elegant solutions at topics such as logging, error checking and data binding.
     * Imagine a solution where
     */
//    val myObservableObject:Procedure by Delegates.observable(Procedure.empty())


}