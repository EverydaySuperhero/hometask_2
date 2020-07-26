package ru.skillbranch.kotlinexample

import android.annotation.SuppressLint
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    private var phone: String? = null
        set(value) {
            if (value == null) return

            normalizePhone(value).also {
                if (!isValidPhone(it))
                    throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
                field = it
            }
        }

    private var _login: String? = null
    var login: String
        @SuppressLint("DefaultLocale")
        set(value) {
            _login = value.toLowerCase()
        }
        get() = _login!!

    private var salt: String? = null
    private lateinit var passwordHash: String

    var accessCode: String? = null

    //for email
    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }

    //For phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor")
        requestAccessCode()
    }

    fun requestAccessCode(): String {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        println("Phone passwordHash is $passwordHash")
        sendAccessCodeToUser(phone!!, code)

        return code
    }

    init {
        println("First init block, primary constructor was called ")
        check(firstName.isNotBlank()) { "FirstName must not be blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "email or phone must not be blank" }
        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    private fun sendAccessCodeToUser(rawPhone: String, code: String) {
        println("..... sending access code: $code on $phone ")

    }

    private fun generateAccessCode(): String {
        val possible = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private fun encrypt(password: String): String {
        if (salt.isNullOrEmpty()) {
            salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        }
        println("Salt while encrypt : $salt")
        return salt.plus(password).md5()

    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray()) // 16 byte
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    fun checkPassword(password: String) = encrypt(password) == passwordHash.also {
        println("Checking passwordHash is $passwordHash")
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) {
            passwordHash = encrypt(newPass)
            if (!accessCode.isNullOrEmpty()) accessCode = newPass
            println("Password $oldPass has been changed to a new password $newPass")
        } else throw IllegalArgumentException("The entered password doesn't much old current password")
    }


    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()
            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email.trim(),
                    password
                )
                else -> throw IllegalArgumentException("Email or phone must not be blank")
            }
        }

        fun isValidPhone(number: String): Boolean = number.matches("\\+?\\d{11}".toRegex())
        fun normalizePhone(number: String): String = number.replace("""[^+\d]""".toRegex(), "")

        private fun String.fullNameToPair(): Pair<String, String?> =
            this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException(
                            "FullName must contain only first and last name, current split " +
                                    "result: ${this@fullNameToPair}"
                        )
                    }
                }
    }
}




