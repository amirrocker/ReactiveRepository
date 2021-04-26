package de.amirrocker.functional

import io.reactivex.rxjava3.core.Observable
import java.time.LocalDate
import java.util.*

/**
 * a very simple Domain class to play with...
 */
data class DomainEntity(
    val id: Int,
    val property:String = ""
) {
    init {
        println("created: $this with id: $id")
    }
}

/**
 * some more abstract theory on domain objects
 */

/*

{
  "id": "123456",
  service : {
      "serviceID": "23454-ID4",
      "serviceName": "23454-NAME4",
      "serviceIP": "23454-IP4",
      "serviceQueue": "23454-QUEUE4",
  }
  "env": "env1",
  "file": "file1",
  "modelParams": {
    "model": "model1",
    "optimizer": "optimizer1",
    "loss": "loss1"
  },
  "log": {
    "log": "log1"
  },
  "hyperParameters": {
    "epochs": "epochs1",
    "learningRate": "learningRate1"
  }
}

 */


typealias TrainingID = Int
typealias TrainingName = String
typealias TrainingDate = Date


/**
 * BETANKUNG
 *
 */

/*

AggregateRoot
    - uid : AggregateID
    - version : Version

CostCenter : AggregateRoot (Kostenstelle)
	- operations : List<OperationID>

Operation (Betankung)
	- operationID : BetankungID
	- datum : Betankungsdatum
	- gear : GearID
	- position : PositionID

Gear (Gerät)
	- gearID : GearID
	- inventarnummer : Inventarnummer
	- seriennummer : Seriennummer
	- kostenstelle : Kostenstelle
	- kennzeichen : Kennzeichen
	- geartype : GearType
	- position : Position
	- operationHours : OperationHours

BetankungDatum
    - id : BetankungdatumID
    - datum : Betankungsdatum
    -

Position
	- id : PositionID
	- lat: Latitude
	- long: Longitude

GearType
    + Baumaschine
    + Zwischentank
    + ManualGear (Kleingerät?)

 */

/**
 * An aggregate is a composition of objects that belong together to form a domain based Entity that 
 * has a function in the domain.
 */
typealias AggregateID = UUID
/**
 * each aggregate should have a version that is sequentially incremented.
 */
typealias Version = Int
/**
 * 
 */
typealias ProcedureID = Long
typealias CostCenterID = Long
typealias GearID = Long
typealias PositionID = Long

typealias Longitude = Long
typealias Latitude = Long

typealias ProcedureDate = LocalDate
typealias InventoryNumber = String
typealias SerialNumber = String
typealias LicensePlate = String
typealias OperationHours = Int
typealias MetervalueBeforeOperation = Double
typealias MetervalueAfterOperation = Double
typealias OperationResult = Double

typealias Index = Int

/**
 * the root of all evil :)
 * The root of our aggregate 
 */
abstract class AggregateRoot(
    val uid: AggregateID,
    val version:Version = -1
) {
    abstract fun empty():AggregateRoot
}

/**
 * CostCenter = Kostenstelle
 */
data class CostCenter(
    val procedures : List<Procedure>
) : AggregateRoot(UUID.randomUUID()) {
    override fun empty():AggregateRoot {
        println("empty implemented as inheritance - compare to using a Delegate to implement empty")
    }
}

data class Procedure(
    val procedureID:ProcedureID,
    val date: ProcedureDate,
    val gear: GearID,
    val metervalueBefore:MetervalueBeforeOperation,
    val metervalueAfter:MetervalueAfterOperation,
    val operationResult:OperationResult
) : AggregateRoot(UUID.randomUUID()) {

    override fun empty():AggregateRoot = Procedure(0L, LocalDate.now(), 0L, 0.0, 0.0, 0.0)


}



data class Gear(
    val gearID: GearID,
    val inventoryNumber : InventoryNumber,
    val serialNumber : SerialNumber,
    val costCenter : CostCenterID,
    val licensePlate : LicensePlate,
    val gearType : GearType,
    val position : Position,
    val operationHours: OperationHours,
    val tankfillLevel : TankfillLevel
): AggregateRoot(UUID.randomUUID()) {
    override fun empty():AggregateRoot = Gear()
}

data class Position(
    val positionID: PositionID,
    val latitude: Latitude,
    val longitude: Longitude
) {}

enum class GearType {
    BAUMASCHINE,
    ZWISCHENTANK,
    MANUALGEAR,
}

/**
 * Options we have with TankfillLevel
 * - Option A: A bottomsheet type selection where the user selects a percent value
 * or selects a NOT_APPLICABLE if he cant determine the tankfillLevel
 * - Option B: A Slider type selection where the user can select from
 * 0 to 100 % in 10 percent steps (ticks)
 */
enum class TankfillLevel {
    NOT_APPLICABLE
}

fun listOfOperations() =
    Observable
        .range(1, 10)
        .map {
            Procedure(
                operationID(),
                operationDate(),
                gearID(it),
                metervalueBefore(),
                metervalueAfter(),
                operationResult()
            )
        }
        .doOnEach {
            println("created ${it.value}")
        }
        .toList().blockingGet()

fun operationResult(): OperationResult = 120.00

fun metervalueAfter(): MetervalueAfterOperation = 330.00

fun metervalueBefore(): MetervalueBeforeOperation = 210.00

fun gearID(index:Int): GearID {
    val gearID = index.toLong()
    addToRepository(createGear(index, gearID))
    return gearID
}

fun createGear(index: Index, gearID: GearID): Gear =
    Gear(
        gearID,
        inventoryNumber(index),
        serialNumber(index),
        costCenter(),
        licensePlate(),
        gearType(),
        position(),
        operationHours(),
        tankfillLevel()
    )

fun tankfillLevel(): TankfillLevel = TankfillLevel.NOT_APPLICABLE

fun operationHours(): OperationHours = 123456789
fun position(): Position = Position(123, 132345L, 12345L )
fun gearType(): GearType = GearType.BAUMASCHINE
fun licensePlate(): LicensePlate = "NRW-AB-1234"
fun costCenter(): CostCenterID = 1234L

fun inventoryNumber(index:Int): InventoryNumber = "Gear $index"
fun serialNumber(index:Int): SerialNumber = "12345-$index"
fun operationDate(): ProcedureDate = LocalDate.now()
fun operationID(): ProcedureID = Long.MAX_VALUE


/**
 * Helper structures
 */
val gearRepository : List<Gear> by lazy {
    mutableListOf()
}

/**
 * Helper functions
 *
 */
fun addToRepository(gear: Gear) {
    (gearRepository as MutableList).add(gear)
}


/**
 * test this
 */
fun logAggregate() {
    val aggregate = testAggregate()
    println(aggregate.toString())

    val firstTankingOperation = aggregate.procedures.get(0)
    firstTankingOperation.gear

}

fun testAggregate() = CostCenter(
    listOfOperations()
)

/*
Sealed class tree for request & response
*/

sealed class Response<T>
data class Success<T>(val t:T) : Response<T>()
data class Error<Throwable>(val t:Throwable) : Response<Throwable>()

fun getPageUrl(url:String):Response<String> {
    if(url.isNotEmpty()) {
        return Success("some Response")
    } else {
        return Error("some Error")
    }
}

fun testResponses() {
    when(getPageUrl("")) {
        is Success -> println("success received")
        is Error -> println("error received")
    }
}

































































