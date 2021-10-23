package de.amirrocker.functional

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO See 90 of 346 - - patterns with kotlin
/**
 * We use the typealias construct to express the actual domain
 * function of each primitive value. That way no Ints or Strings
 * are floating around - DDD dogma :)
 */
typealias WeaponDamage = Int
typealias DistanceTraveled = Int
typealias SupportGiven = Int
typealias ResearchDone = Int
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

interface ScienceUnit : Unit {
    override fun attack(longitude: Longitude, latitude: Latitude) {
        println("ScienceUnit does minimal damage of 5")
    }

    fun research():ResearchDone
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

interface Wheels : Legs {
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

enum class ResearchUnits {
    Researcher,
    LaboratoryTech,
    FieldOperative
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

// TODO add other gadgets
class Scientist(
    val legs: Legs
) : ScienceUnit, Infantry {

    override fun move(longitude: Longitude, latitude: Latitude) {
        val distanceTraveled = legs.move()
        println("Soldier moved: $distanceTraveled m")
    }

    override fun research(): ResearchDone {
        println("no research done - not yet implemented")
        return 0
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
        println("TANK attacked enemy for damage: $damagedCaused")
    }

    override fun move(longitude: Longitude, latitude: Latitude) {
        val distanceTraveled = wheels.move()
        println("TANK moved: $distanceTraveled m")
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

class RegimentalHeadquarter : Headquarters {

    override val buildings: List<Building<*, Unit>> by lazy {
            mutableListOf()
    }

    fun buildBarracks() : Barracks = Barracks().also { building -> (buildings as MutableList).add(building) }

    fun buildVehicleFactory(): VehicleFactory = VehicleFactory().also { factory -> (buildings as MutableList).add(factory) }

}

interface Building<in UnitType, out ProducedUnit> where UnitType : Enum<*>, ProducedUnit : Unit {
    fun build(type:UnitType):ProducedUnit
}

interface ScienceBuilding<in UnitType, out ProducedUnit> where UnitType : Enum<*>, ProducedUnit : ScienceUnit {
    fun build(type:UnitType):ProducedUnit
}


sealed class ResearchCenter : ScienceBuilding<ResearchUnits, Scientist> {
    override fun build(type: ResearchUnits): Scientist =
        when(type) {
            ResearchUnits.Researcher -> buildResearcher()
            ResearchUnits.FieldOperative -> buildFieldOperative()
            ResearchUnits.LaboratoryTech -> buildLaboratoryTech()
        }

    private fun buildResearcher(): Scientist = Scientist(AthleticLegs())
    private fun buildFieldOperative(): Scientist = Scientist(AthleticLegs())
    private fun buildLaboratoryTech(): Scientist = Scientist(AthleticLegs())

}

class Barracks : Building<InfantryUnits, Infantry> {
    override fun build(type: InfantryUnits): Infantry =
        when(type) {
            InfantryUnits.Rangers -> buildRanger()
            InfantryUnits.Grenadiers -> buildGrenadiers()
            InfantryUnits.Medics -> buildMedics()
            InfantryUnits.Robocops -> buildRobocops()
        }

    private fun buildRanger() = Soldier(M4AssaultRifle(), RegularLegs())
    private fun buildGrenadiers() = Soldier(GrenadeLauncher(), AthleticLegs())
    private fun buildMedics() = Soldier(M4AssaultRifle(), ExoSkeletonLegs())
    private fun buildRobocops() = Soldier(GrenadeLauncher(), ExoSkeletonLegs())
}

class VehicleFactory : Building<MechanizedUnits, Vehicle> {

    override fun build(type: MechanizedUnits): Vehicle =
        when(type) {
            MechanizedUnits.APC -> createAPC()
            MechanizedUnits.IFV -> createIFV()
            MechanizedUnits.SPAAG -> createSPAAG()
            MechanizedUnits.TANK -> createTank()
            MechanizedUnits.SUPPLY -> createSupply()
        }

    private fun createAPC(): APC = APC(GrenadeLauncher())

    private fun createIFV(): APC = APC(GrenadeLauncher()) // TODO give IFV a TOW missile

    private fun createSPAAG(): APC = APC(GrenadeLauncher()) // TODO a 4 x Barrel 0.5 Cal :)

    private fun createTank(): Tank = Tank(GrenadeLauncher()) // TODO a 110 mm smoothbore FSAPDS / HEATFS :) :)

    private fun createSupply(): APC = APC(M4AssaultRifle()) // TODO think on how to represent supply and training

}

typealias NumberOfColumns = Int
typealias NumberOfRows = Int

interface Map {

    data class Board(
        val columns:NumberOfColumns,
        val rows:NumberOfRows,
    ) : Map {
        override val board: Board
            get() = this
    }

    val board:Board

    fun getSize() = board.columns * board.rows
}



// test method
fun createSoldierGame(vararg args:String) = runBlocking {

        // coroutine Builder
        launch {

            // special suspend function
            delay(500)

            println(
//                 suspend function
                simpleSequence().forEach { println(it) }
            )
        }

        async {
            startGameTime().collect { value -> println("gametime value $value") }
        }

        val board = Map.Board(64, 64)
        println(board.getSize())


        val barracks = RegimentalHeadquarter().buildBarracks()
        val vehicleFactory = RegimentalHeadquarter().buildVehicleFactory()

        println("$barracks created")
        println("$vehicleFactory created")

        val ranger = barracks.build(InfantryUnits.Rangers)
        ranger.move(0L, 0L)
        ranger.attack(0L, 0L)

        val tank = vehicleFactory.build(MechanizedUnits.TANK)
        tank.move(0L, 0L)
        tank.attack(0L, 0L)

        launch {
            simulateSimpleAIMoves(tank).collect { value -> println("AI moved $value") }
        }
    }




fun startGameTime():Flow<Int> = flow {
    (0 until 60).forEach {
        delay(1000)
        emit(it)
    }
}

fun simulateSimpleAIMoves( tank:Vehicle ):Flow<Int> = flow {
    (0 until 60).forEach {
        delay(1000)
        tank.move(0L, 0L)
        emit(it)
    }
}



// playing with flow

suspend fun simpleSequence() = sequence { (0..5).forEach {
//    Thread.sleep(2000)
    yield(it)
} }

















































