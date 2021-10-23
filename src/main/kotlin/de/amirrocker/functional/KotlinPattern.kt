package de.amirrocker.functional

// TODO See 90 of 346 - - patterns with kotlin
/**
 * We use the typealias construct to express the actual domain
 * function of each primitive value. That way no Ints or Strings
 * are floating around - DDD dogma :)
 */
typealias WeaponDamage = Int
typealias DistanceTraveled = Int
typealias SupportGiven = Int
typealias Longitude = Long
typealias Latitude = Long

typealias BarracksName = String
typealias FactoryName = String


/**
 * Note: const values are not allowed inside a class unless
 * they are wrapped inside a object.
 */
const val WEAPON_DAMAGE_DEFAULT : WeaponDamage = 100
const val GRENADE_WEAPON_DAMAGE : WeaponDamage = 250
const val M4_ASSAULT_RIFLE_WEAPON_DAMAGE : WeaponDamage = 175

const val EXO_SKELETON_LEGS_TRAVELING_DISTANCE: DistanceTraveled = 175
const val REGULAR_LEGS_TRAVELING_DISTANCE : DistanceTraveled = 50

const val DEFAULT_LEAP_FROG_DISTANCE = 500


/**
 * Each entity is represented as a Unit
 */
interface Unit {
    fun attack(longitude: Longitude, latitude: Latitude)
    fun move(longitude: Longitude, latitude: Latitude)
}

/**
 * support units such as medics or supply and repair trucks
 */
interface Support : Unit {
    fun support():SupportGiven
}

/**
 * regular grunt infantry
 */
interface Infantry : Unit

/**
 * each mechanized unit is a Vehicle
 */
interface Vehicle : Unit

/**
 * this should be renamed :)
 * TODO rethink the name
 */
interface Legs {
    fun move():DistanceTraveled
}

sealed interface Wheels : Legs {
    object DEFAULT_MOTOR : Wheels {
        override fun move(): DistanceTraveled = DEFAULT_LEAP_FROG_DISTANCE // the default travel distance for wheeled units
    }
}

interface Weapon {
    fun causeDamage():WeaponDamage
}

enum class InfantryUnits {
    Rangers,
    Grenadiers,
    Medics, // support Unit
    Robocops
}

enum class MechanizedUnits {
    TANK,
    APC,
    IFV,
    SPAAG,
    SUPPLY, // support Unit
}

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
        println("Soldier attacked enemy for damage: $damageCaused hp")
    }

    override fun move(longitude: Longitude, latitude: Latitude) {
        // compute facing
        // compute direction
        // move at speed
        val distanceTraveled = legs.move()
        println("Soldier moved: $distanceTraveled m")
    }
}

/**
 * The Vehicle classes
 */
class APC(
    val weapon: Weapon,
    val wheels: Wheels = Wheels.DEFAULT_MOTOR
) : Vehicle {

    override fun attack(longitude: Longitude, latitude: Latitude) {
        val damagedCaused = weapon.causeDamage()
        println("APC attacked enemy for damage: $damagedCaused")
    }

    override fun move(longitude: Longitude, latitude: Latitude) {
        val distanceTraveled = wheels.move()
        println("APC moved: $distanceTraveled m")
    }
}

class Tank(
    val weapon: Weapon,
    val wheels: Wheels = Wheels.DEFAULT_MOTOR
) : Vehicle {
    override fun attack(longitude: Longitude, latitude: Latitude) {
        val damagedCaused = weapon.causeDamage()
        println("APC attacked enemy for damage: $damagedCaused")
    }

    override fun move(longitude: Longitude, latitude: Latitude) {
        val distanceTraveled = wheels.move()
        println("APC moved: $distanceTraveled m")
    }
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
// TEST ONLY - use Barracks to create units
val rifleman = Soldier(M4AssaultRifle(), AthleticLegs())
val grenadier = Soldier(GrenadeLauncher(), RegularLegs())
val robosoldier = Soldier(GrenadeLauncher(), ExoSkeletonLegs())
// TEST ONLY - use Barracks to create units





/**
 * sweet - now lets start adding some buildings
 */
interface Headquarters {
    val buildings:List<Building<*, Unit>>
}




//class RegimentalHeadquarter : Headquarters {
//
////    override val buildings: List<Building<*, Unit>> by lazy {
////            MutableList<Building<*, Unit>>(0) { Building("some", "",
////                Soldier( M4AssaultRifle(), AthleticLegs() )
////            )
////        }
////    }
////        get() = field
//
//
//    fun buildBarracks() : Barracks = Barracks("someName").also { building -> (buildings as MutableList).add(building) }
//
//    fun buildVehicleFactory(): VehicleFactory = VehicleFactory("factoryName")
//
//}

//data class VehicleFactory(
//    val factoryName:FactoryName
//) {
//
//}

interface Building<in UnitType, out ProducedUnit> where UnitType : Enum<*>, ProducedUnit : Unit {
    fun build(type:UnitType):ProducedUnit
}


class Barracks : Building<InfantryUnits, Infantry> {
    override fun build(type: InfantryUnits): Infantry =
        when(type) {
            InfantryUnits.Rangers -> buildRanger()
            InfantryUnits.Grenadiers -> buildGrenadiers()
            InfantryUnits.Medics -> buildMedics()
            InfantryUnits.Robocops -> buildRobocops()
        }

    fun buildRanger() = Soldier(M4AssaultRifle(), RegularLegs())
    fun buildGrenadiers() = Soldier(GrenadeLauncher(), AthleticLegs())
    fun buildMedics() = Soldier(M4AssaultRifle(), ExoSkeletonLegs())
    fun buildRobocops() = Soldier(GrenadeLauncher(), ExoSkeletonLegs())
}


class VehicleFactory : Building<MechanizedUnits, Vehicle> {

    override fun build(type: MechanizedUnits): Vehicle =
        when(type) {
            MechanizedUnits.APC ->
            MechanizedUnits.IFV ->
            MechanizedUnits.SPAAG ->
            MechanizedUnits.TANK ->
            MechanizedUnits.SUPPLY ->
        }


}






// test method
fun createSoldierGame(vararg args:String) {

    rifleman.attack(Longitude.MIN_VALUE, Latitude.MIN_VALUE)
    grenadier.move(Longitude.MIN_VALUE, Latitude.MIN_VALUE)
    robosoldier.attack(Longitude.MIN_VALUE, Latitude.MIN_VALUE)


}


















































