package de.amirrocker.functional

// TODO See 90 of 346 - - patterns with kotlin
/**
 * We use the typealias construct to express the actual domain
 * function of each primitive value. That way no Ints or Strings
 * are floating around - DDD dogma :)
 */
typealias WeaponDamage = Int
typealias DistanceTraveled = Int
//typealias Longitude = Long
//typealias Latitude = Long


/**
 * Note: const values are not allowed inside a class unless
 * they are wrapped inside a object.
 */
const val WEAPON_DAMAGE_DEFAULT : WeaponDamage = 100
const val GRENADE_WEAPON_DAMAGE : WeaponDamage = 250
const val M4_ASSAULT_RIFLE_WEAPON_DAMAGE : WeaponDamage = 175

const val EXO_SKELETON_LEGS_TRAVELING_DISTANCE: DistanceTraveled = 175
const val REGULAR_LEGS_TRAVELING_DISTANCE : DistanceTraveled = 50


class Soldier(
    private val weapon:Weapon,
    private val legs:Legs
) : Infantry {

    // just to make the point....
    object VALUES {
        const val SOME_VALUE = 99.0
    }

    override fun attack(longitude: Longitude, latitude: Latitude) {
        // find suitable target
        // aim
        // shoot
        val damageCaused = weapon.causeDamage()
    }

    override fun move(longitude: Longitude, latitude: Latitude) {
        // compute facing
        // compute direction
        // move at speed
        val distanceTraveled = legs.move()
    }
}

interface Infantry {

    fun attack(longitude: Longitude, latitude: Latitude)

    fun move(longitude: Longitude, latitude: Latitude)

}

interface Legs {
    fun move():DistanceTraveled
}

interface Weapon {
    fun causeDamage():WeaponDamage
}

/**
 * The weapon classes
 */
class GrenadeLauncher : Weapon {
    override fun causeDamage(): WeaponDamage = GRENADE_WEAPON_DAMAGE
}

class M4AssaultRifle : Weapon {
    override fun causeDamage(): WeaponDamage = M4_ASSAULT_RIFLE_WEAPON_DAMAGE
}

/**
 * the movement classes
 * with future soldiers :)
 */
class ExoSkeletonLegs : Legs {
    override fun move(): DistanceTraveled = EXO_SKELETON_LEGS_TRAVELING_DISTANCE
}

class RegularLegs : Legs {
    override fun move(): DistanceTraveled = REGULAR_LEGS_TRAVELING_DISTANCE
}

class AthleticLegs : Legs {
    override fun move(): DistanceTraveled = REGULAR_LEGS_TRAVELING_DISTANCE * 2
}

/**
 * the above hierarchy has transformed a complex layered approach TODO show layered version
 * with a simple shallow approach that is easily maintainable.
 */

val rifleman = Soldier(M4AssaultRifle(), AthleticLegs())
val grenadier = Soldier(GrenadeLauncher(), RegularLegs())
val robosoldier = Soldier(GrenadeLauncher(), ExoSkeletonLegs())


/**
 *
 */



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






















































