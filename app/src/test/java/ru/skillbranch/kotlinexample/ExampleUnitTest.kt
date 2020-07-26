package ru.skillbranch.kotlinexample

import org.junit.After
import org.junit.Test

import org.junit.Assert.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    /**
        Добавьте метод в UserHolder для очистки значений UserHolder после выполнения каждого теста,
        это необходимо чтобы тесты можно было запускать одновременно

            @VisibleForTesting(otherwise = VisibleForTesting.NONE)
            fun clearHolder(){
                map.clear()
            }
    */
    @After
    fun after(){
        UserHolder.clearHolder()
    }

    @Test
    fun register_user_success() {
        val user = UserHolder.registerUser(
            "John Doe",
            "John_Doe@unknown.com",
            "testPass"
        )
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        assertEquals(expectedInfo, user.userInfo)
        UserHolder.clearHolder()
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_blank() {
        UserHolder.registerUser(
            "",
            "John_Doe@unknown.com",
            "testPass"
        )
        UserHolder.clearHolder()

    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_name() {
        UserHolder.registerUser(
            "John Jr Doe",
            "John_Doe@unknown.com",
            "testPass"
        )
        UserHolder.clearHolder()

    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_exist() {
        UserHolder.registerUser(
            "John Doe",
            "John_Doe@unknown.com",
            "testPass"
        )
        UserHolder.registerUser(
            "John Doe",
            "John_Doe@unknown.com",
            "testPass"
        )
        UserHolder.clearHolder()
    }

    @Test
    fun register_user_by_phone_success() {
        val user = UserHolder.registerUserByPhone(
            "John Doe",
            "+7 (917) 971 11-11"
        )
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        assertEquals(expectedInfo, user.userInfo)
        assertNotNull(user.accessCode)
        assertEquals(6, user.accessCode?.length)
        UserHolder.clearHolder()
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_blank() {
        UserHolder.registerUserByPhone(
            "",
            "+7 (917) 971 11-11"
        )
        UserHolder.clearHolder()
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_illegal_name() {
        UserHolder.registerUserByPhone(
            "John Doe",
            "+7 (XXX) XX XX-XX"
        )
        UserHolder.clearHolder()
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_by_phone_illegal_exist() {
        UserHolder.registerUserByPhone(
            "John Doe",
            "+7 (917) 971-11-11"
        )
        UserHolder.registerUserByPhone(
            "John Doe",
            "+7 (917) 971-11-11"
        )
        UserHolder.clearHolder()
    }

    @Test
    fun login_user_success() {
        UserHolder.registerUser(
            "John Doe",
            "John_Doe@unknown.com",
            "testPass"
        )
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        val successResult = UserHolder.loginUser(
            "john_doe@unknown.com",
            "testPass"
        )

        assertEquals(expectedInfo, successResult)
        UserHolder.clearHolder()
    }

    @Test
    fun login_user_by_phone_success() {
        val user = UserHolder.registerUserByPhone(
            "John Doe",
            "+7 (917) 971-11-11"
        )
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult = UserHolder.loginUser(
            "+7 (917) 971-11-11",
            user.accessCode!!
        )

        assertEquals(expectedInfo, successResult)
        UserHolder.clearHolder()
    }

    @Test
    fun login_user_fail() {
        UserHolder.registerUser(
            "John Doe",
            "John_Doe@unknown.com",
            "testPass"
        )

        val failResult = UserHolder.loginUser(
            "john_doe@unknown.com",
            "test"
        )

        assertNull(failResult)
        UserHolder.clearHolder()
    }

    @Test
    fun login_user_not_found() {
        UserHolder.registerUser(
            "John Doe",
            "John_Doe@unknown.com",
            "testPass"
        )

        val failResult = UserHolder.loginUser(
            "john_cena@unknown.com",
            "test"
        )

        assertNull(failResult)
        UserHolder.clearHolder()
    }

    @Test
    fun request_access_code() {
        val user = UserHolder.registerUserByPhone(
            "John Doe",
            "+7 (917) 971-11-11"
        )
        val oldAccess = user.accessCode
        UserHolder.requestAccessCode("+7 (917) 971-11-11")

        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult = UserHolder.loginUser(
            "+7 (917) 971-11-11",
            user.accessCode!!
        )

        assertNotEquals(oldAccess, user.accessCode!!)
        assertEquals(expectedInfo, successResult)
        UserHolder.clearHolder()
    }
}