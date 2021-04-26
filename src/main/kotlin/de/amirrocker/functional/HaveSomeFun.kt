package de.amirrocker.functional

/**
 * Or have some Functor :)
 * What are Functors, Monads and Applicatives?
 *
 * "Simple, a Monad is a monoid in the category of endofunctors. Alright? :)"
 *
 * What this covers are
 * - Functors
 * - Options, lists, and functions as functors
 * - Monads
 * - Applicatives
 *
 * Let's look at some code:
 */
fun main(args:Array<String>) {

    /**
     * looking at what happens here is:
     * multiply a value, transform to string and print out.
     * nothing spectacular. And this is a Functor.
     * Where only the internal value changes but the external type
     * stays the same. All transformations have happened on the list.
     * A Functor is type that defines a way to transform or map its content.
     *
    listOf(1, 2, 3)
        .map { it * it }
        .map(Int::toString)
        .forEach(::println)

//     these are "test calls" for the concepts described below.

    val some = Option.Some("kotlin").map {
        println("logged mapped to $it")
        "it : $it"
    }
    println("println: Option.Some = $some")

    val none = Option.None.map(String::toUpperCase)
    println("None: $none")

    testExtensionFunction()

    whatIsAMonad()

    val product = calculateProductPrice(Option.Some("productA"))
    println("product: $product")

    lookieHere()

    anAppliedApplicative()
    */



}

/**
 * if we were to use Scala or Haskell we could define a Functor as
 * a real Type like so: (wont compile in kotlin so it is pseudo code)
 *
interface Functor<C<_>> {
    fun <A, B> map(ca:C<A>, transform: (A)->B):C<B>
}

trait Functor[F[_]] extends Invariant[F] {
    self => def map[A,B](fa: F[A])(f: A => B): F[B]
    // and more code here....
}
 */

/**
 * In kotlin there is no mechanism to implement or use these features. But they
 * can be simulated by using convention and extension functions.
 * "If a type has a function or extension function then map is a functor
 * Look at an Option implementation:
 * and the simple tests in main()
 */

sealed class Option<out T> {
    object None: Option<Nothing>() {
        override fun toString() = "None"
    }
    data class Some<out T>(val value: T) : Option<T>() {}
    /** to see what this companion object is all about look at the paragraph following anAppliedApplicative */
    companion object
}

fun <T,R> Option<T>.map(transform:(T)->R):Option<R> =
    when(this) {
        Option.None -> Option.None
        is Option.Some -> Option.Some(transform(value))
    }

/**
 * Extension functions are flexible enough to allow writing map functions for
 * arbitrary types. Look at this:
 */
fun <A, B, C> ((A)->B).map(transform:(B)->C):(A)->C = { t -> transform(this(t)) }

fun testExtensionFunction() {
    val double = { a:Int -> "${a*a}" }.map<Int,String, Double> { stringValue: String ->
        stringValue.toDouble()
    }
    println("double: ${ double(2) }")

    val add3AndMulBy2: (Int) -> Int = { intValue: Int -> intValue + 3 }.map { intValue: Int -> intValue * 2 }
    println("add3AndMulBy2: ${add3AndMulBy2(3)}")

}

/**
 * A Monad is a Functor Type that defines a flatMap function (bind in other languages) that receives a lambda
 * and returns the same type.
 * look at a regular list:
 */
fun whatIsAMonad() {

    val result = listOf(1,2,3,4)
        .flatMap {
            listOf(it*2, it * it)
        }
        .joinToString()
    println("result: $result") // prints 2, 1, 4, 4, 6, 9, 8, 16
}

/**
 * now, after establishing what a Monad is, lets make a Monad out of our Option.
 */
fun <T,R> Option<T>.flatMap(fm:(T)->Option<R>):Option<R> =
    when(this) {
        Option.None -> Option.None
        is Option.Some -> fm(value)
    }

/**
 * rewriting the map method using the very similar flatMap.
 *
 */
fun <T,R> Option<T>.mapWithFlatMap(transform: (T) -> R):Option<R> =
    flatMap { t -> Option.Some(transform(t)) }

/**
 * Now flatMap can be used in more creative ways than possible with map.
 * Look at this example:
 */
fun calculateProductPrice(product:Option<String>) : Option<Double> =
    product.flatMap {
        if (it.equals("productA")) {
            Option.Some(50.0)
        } else if( it.equals("productB") ) {
            Option.Some(100.0)
        } else {
            Option.None
        }
    }

fun lookieHere() {
    val maybe1 = Option.Some(40)
    val maybe2 = Option.Some(20)

    maybe1.flatMap { outerValue:Int ->
        maybe2.flatMap { innerValue: Int ->
            println("inner * outer")
            Option.Some(outerValue * innerValue)
        }
    }

    /** a bit shorter even using flatMap and map */
    maybe1.flatMap { outer:Int ->
        maybe2.map { inner:Int ->
            outer * inner
        }
    }

    /**
     * and finally the first piece of code could also be
     * rewritten more sly like this:
     * imagine you write a program that converts from single values to double, square and cubic values,
     * for shipping containers maybe ? :)))
     */
    val numbers = listOf(1, 2, 3, 4)
    val functions = listOf<(Int)->Int>({value:Int -> value * 2}, {value:Int -> value * value}, {value:Int -> value * value * value })

    val result = numbers.flatMap { number:Int ->
        functions.map {
            it(number)
        }
    }
    .joinToString()
}

/**
 * Now after we have looked at Monads, lambdas inside wrappers with a parameter inside the same kind of wrapper,
 * we look at Applicatives.
 * An Applicative is a type that defines two functions:
 * - a pure(t:T) function that returns the T value wrapped in the Applicative Type (an Observable is a type of
 * Applicative, actually a Monad, but as we can see below a Monad is a more "specialized" form of Applicative. )
 * - an ap or apply(lambda:(T)->R) function that receives a lambda wrapped in the applicative type.
 * A pseudo hierarchy in "functional speak" of a generic Applicative up to the Monad may look like so:
 *
interface Functor<C<_>> {
    fun <A,B> map(ca:C<A>), transform:(A)->B):C<B>
}

interface Applicative<C<_>>: Functor<C> {
    fun <A> pure(a:A):C<A>

    fun <A, B> apply(ca:C<A>, fab:C<(A) -> B>):C<B>
}

interface Monad<C<_>> : Applicative<C> {
    fun <A,B> flatMap(ca:C<A>, transform:(A) -> C<B>):C<B>
}

 * "In short, an Applicative is a more powerful Functor and a Monad a more powerful Applicative."
 * Lets look at it with an example:
 * an apply extension function for a List
 */
fun <T,R> List<T>.ap(fab:List<(T)->R>):List<R> =
    fab.flatMap {
        this.map(it)
    }

fun anAppliedApplicative() {
    val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val functions = listOf<(Int)->Int>({i:Int -> i * 2}, {i:Int -> i * i}, {i:Int -> i * i * i })

    val result = list.flatMap { outer: Int ->
        functions.map { function: (Int) -> Int ->
            function(outer)
        }
    }
    .joinToString()

    /**
     * now with the new ap function we can rewrite the Applicative like this:
     * Note: This solution has a different Order than the one before.
     * see if we can spot why....
     */
    val moreDescriptiveResult = list
        .ap(functions)
        .joinToString()
    println("a more descriptive result using the ap(apply) function: $moreDescriptiveResult")

}

/**
 * Now we go ahead and try to add the newly aquired wisdom to our Option class.
 * add a pure and apply extension function to Option:
 *
fun <T> Option.Companion.pure(t:T):Option<T> = GO ON HERE
*/





















