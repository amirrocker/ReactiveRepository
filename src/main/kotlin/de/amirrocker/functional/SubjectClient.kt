package de.amirrocker.functional

class SubjectClient {

    fun main(vararg args:String) {
        println("Main.main() called with args")
        Flowables().playWith()
        playWithSubjects()
    }

}

// Annotations - check the AnnotationProcessor project.

@Target(AnnotationTarget.CLASS) // annotation can be used on classes, interfaces, objects
@Retention(AnnotationRetention.RUNTIME) // annotation can be queried at runtime
annotation class Tasty

/* annotations can have parameters, with one limitation, they cannot be null */
@Target(AnnotationTarget.CLASS) // annotation can be used on classes, interfaces, objects
@Retention(AnnotationRetention.RUNTIME) // annotation can be queried at runtime
annotation class TastyWithParameter(val isReallyTasty:Boolean = true)

interface Oven {
    fun bake()
}

@TastyWithParameter(false)
object StoneOven : Oven {
    override fun bake() {
        println("currently just baking ... without a product to be baked .... ")
    }
}


fun annotationsTesting() {
}