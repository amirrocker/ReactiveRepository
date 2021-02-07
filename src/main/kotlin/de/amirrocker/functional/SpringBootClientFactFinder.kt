package de.amirrocker.functional

import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate

val userEntity : String by lazy {
    "USERS"
}

val factEntity : String by lazy {
    "FACTS"
}

enum class Gender {
    MALE, FEMALE;

    companion object {
        fun valueOfIgnoreCase(name: String) = valueOf(name.toUpperCase())
    }
}

typealias UserId = Int
typealias Firstname = String
typealias Lastname = String

data class User(
    val userId: UserId,
    val firstname: Firstname,
    val lastname: Lastname,
    val gender: Gender
)

data class Fact(
    val id: Int,
    val value: String,
    val user: User? = null
)

/**
 * get a Chuck-Norris style fact about the user.
 * The Internet Chuck Norris Database API (http://www.icndb.com/api) is the source of the
 * chuck fact.
 */
interface UserService {
    fun getFact(id: UserId):Fact
}

interface UserClient {
    fun getUser(id: UserId):User
}

interface FactClient {
    fun getFact(user: User):Fact
}

/**
 * a simple, naive WebClient base class.
 */
abstract class WebClient {
    protected val apacheClient = ApacheClient()

    protected val gson = GsonBuilder()
            .registerTypeAdapter<User> {
                deserialize { deserializerArg ->
                        val json = deserializerArg.json
                        User(
                            json["info"]["seed"].int,
                            json["results"][0]["firstname"].string.capitalize(),
                            json["results"][0]["lastname"].string.capitalize(),
                            Gender.valueOfIgnoreCase(
                                json["results"][0]["gender"].string
                            )
                        )
                }
            }
            .registerTypeAdapter<Fact> {
                deserialize { deserializerArg ->
                        val json = deserializerArg.json
                        Fact(json["value"]["id"].int, json["value"][0]["joke"].string)
                }
            }
            .create()
}

class Http4kUserClient : WebClient(), UserClient {
    override fun getUser(id: UserId): User {
        return gson
                .fromJson(
                    apacheClient(
                        Request(
                            Method.GET,
                            "https://randomuser.me/api"
                        )
                            .query("seed", id.toString())
                    )
                        .bodyString()
                )
    }
}

class Http4kFactClient : WebClient(), FactClient {
    override fun getFact(user: User): Fact {
        return gson
                .fromJson<Fact>(
                    apacheClient(
                        Request(
                            Method.GET,
                            "https://wwww.icndb.com/jokes/random"
                        )
                            .query("firstName", user.firstname)
                            .query("lastName", user.lastname)
                    )
                        .bodyString()
                )
                .copy(user = user) // Note: the compiler can't infer the type - <Fact> needed!
    }
}

class MockUserClient : UserClient {
    override fun getUser(id: UserId): User {
        Thread.sleep(1000)
        return User(id, "FakeMockFirstname", "FakeMockLastname", Gender.FEMALE)
    }
}

class MockFactClient : FactClient {
    override fun getFact(user: User): Fact {
        return Fact(user.userId, "FACT 4 ${user.firstname} ${user.lastname}", user)
    }
}

/* db access */
interface UserRepository {
    fun getUserById(id: UserId):User?
    fun saveUser(user: User):Int
}

interface FactRepository {
    fun getFactByUserId(id: UserId):Fact?
    fun saveFact(fact: Fact)
}

/* I am lazy - TODO MOVE TO DATA LAYER!!!! */
/* TODO -- Refactor the app to use a separate database strategies */
/*  */

interface RepositoryStrategy {
    fun getRepository():Repository
}

interface Repository {
    fun <T> toNullable(block: () -> T):T?
}

/**
* for database we use the jdbc framework from spring which is a simple dependency and
* since superbly engineered! is a joy to study! we can use spring.io initializer to get the
* dependency. https://start.spring.io/
*  */

abstract class JdbcRepository(
    protected val template: JdbcTemplate
) : Repository {
    /* simple way to transform an empty result into a Nullable */
    override fun <T> toNullable(block: () -> T):T? {
        return try {
            block()
        } catch (ex: EmptyResultDataAccessException) {
            error("Error : EmptyResultDataAccessException: $ex")
        }
    }
}


class JdbcUserRepository(
    template: JdbcTemplate
) : JdbcRepository(template), UserRepository {

    override fun getUserById(id: UserId): User? {
        return toNullable {
            template.queryForObject(
                "SELECT", arrayOf<Any>(id)
            ) { resultSet, numRow ->
                User(
                    resultSet.getInt("ID"),
                    resultSet.getString("FIRST_NAME"),
                    resultSet.getString("LAST_NAME"),
                    Gender.valueOfIgnoreCase(resultSet.getString("GENDER"))
                )
            }
        }

    }

//    val UPDATE_SQL_VALUE_DEFINITION = "INSERT INTO USERS VALUES(?,?,?,?)"
    val UPDATE_SQL_VALUE_DEFINITION = "INSERT INTO $userEntity VALUES(?,?,?,?)"

    override fun saveUser(user: User): Int =
        template.update(UPDATE_SQL_VALUE_DEFINITION,
                user.userId,
                user.firstname,
                user.lastname,
                user.gender.name
            )
}

class JdbcFactRepository(
    template: JdbcTemplate
) : JdbcRepository(template), FactRepository {

    val SQL = "SELECT * FROM USERS as U inner join FACTS as F on U.ID = F.USER where U.ID=?"
    val VALUE = "VALUE"
    val USER = "USER"

    /* TODO NOTE: Rather than using  */
    override fun getFactByUserId(id: Int): Fact? = toNullable {
            template.queryForObject(SQL, arrayOf<Any>(id)) { resultSet, numRow ->
                with(resultSet) {
                    Fact(
                        getInt(5),
                        getString(6),
                        User(
                            getInt(1),
                            getString(2),
                            getString(3),
                            Gender.valueOfIgnoreCase(getString(4))
                        )
                    )
                }
            }
        }


    /* TODO Collect into one single location ... */
    val UPDATE_SQL_VALUE_DEFINITION = "INSERT INTO $factEntity VALUES(?,?,?)"

    override fun saveFact(fact: Fact) {
        template.update(UPDATE_SQL_VALUE_DEFINITION,
            Fact(
                fact.id,
                fact.value,
                fact.user?:User(0,
                    "FirstnameTest",
                    "LastnameTest",
                    Gender.MALE)
            )
        )
    }
}

/**
 * If we were to use a regular kotlin console app on the desktop for example we may use
 * an H2 db or such but since we are using Android and we do have the sqlite db available
 * already we should rather connect the used jdbc with the native sqlite db than using a
 * third party implementation like H2.
 *
 */
fun initJdbcTemplate():JdbcTemplate {
    return JdbcTemplate(
        DataSourceBuilder.create()
            .driverClassName("")
            .url("")
            .username("")
            .password("")
            .build()
    ).apply {
        execute("CREATE TABLE USERS .... A long sql statement to actually create the tables.")
    }
}





/**
 *
 *
 */
class MockUserRepository : UserRepository {
    private val users = hashMapOf<UserId, User>()

    override fun getUserById(id: UserId): User? {
        println("MockUserRepository.getUserById called ... ")
        Thread.sleep(1000)
        return users[id]
    }

    override fun saveUser(user: User): Int {
        println("saveUser: $user")
        Thread.sleep(1000)
        users[user.userId]=user
        return 0
    }
}


class MockFactRepository : FactRepository {
    private val facts = hashMapOf<Int, Fact>()

    override fun getFactByUserId(id: UserId): Fact? {
        println("getFactByUserId called for id: $id ")
        Thread.sleep(1000)
        return facts[id]
    }

    override fun saveFact(fact: Fact) {
        println("saving fact: $fact")
        Thread.sleep(1000)
        facts[fact.user?.userId ?: 0] = fact
    }
}

/**
 * TODO THIS BELONGS INTO THE DOMAIN LAYER AGAIN?
 *
 * We start with a synchronous implementation since synchronous code / implementations are
 * mostly the easier solution to write and to test. But it does not use system resources in
 * the most efficient manner. TODO PROOF ! Pro - Contra list
 *
 */
class SynchronousUserService(
    private val userClient:UserClient,
    private val factClient:FactClient,
    private val userRepository: UserRepository,
    private val factRepository: FactRepository
) : UserService {

    override fun getFact(id: UserId): Fact {
        val user = userRepository.getUserById(id)
        return if(user == null) {
            val userFromService = userClient.getUser(id)
            userRepository.saveUser(userFromService)
            getFact(userFromService.userId)
        } else {
            factRepository.getFactByUserId(user.userId) ?: getFact(user)
        }
    }

    private fun getFact(user:User): Fact {
        val fact = factClient.getFact(user)
        factRepository.saveFact(fact)
        return fact
    }
}

/* Note that this is supposed to be as generic as possible */
fun mainSynchronous() {

}











































