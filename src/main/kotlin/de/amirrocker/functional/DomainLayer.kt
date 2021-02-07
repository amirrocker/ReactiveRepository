package de.amirrocker.functional

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