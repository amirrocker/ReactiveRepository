package de.amirrocker.functional

/**
 * Pseudo - Decorator
 * it checks for the existence of the key and if found
 * does something - in this case simply print key and value.
 * Pseudo decorator since it works with inheritance only
 * and not with composition like a real decorator.
 */
fun createEmail() {

    val map = HappyMap<String, String>()
    map["one"] = "one"
    map["two"] = "two"
    map["two"] = "three"
    map["three"] = "four"
    map["one"] = "five"
    map["two"] = "six"


}


/**
 * enhance class by simply adding a log statement anytime
 * a key has been overridden.
 * -
 */
class HappyMap<K,V> : HashMap<K, V>() {

    override fun put(key: K, value: V): V? {
        return super.put(key, value).apply {
            this?.let {
                //println("yay, this: $this and key: $key and value: $value and it: $it")
                println("yay, key: $key and value: $value")
            }
        }
    }
}

