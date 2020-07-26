package ru.skillbranch.kotlinexample

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User = User.makeUser(fullName, email, password)
        .also { user -> addUser(user) }


    private fun addUser(user: User) {
        if (map.containsKey(user.login))
            throw IllegalArgumentException("A user with this email already exists")
        map[user.login] = user
    }


    private fun loginUserByPhone(phoneNumber: String, authCode: String): User? =
        map[User.normalizePhone(phoneNumber)]?.let {
            if (it.accessCode == authCode)
                it
            else null
        }

    private fun loginUserByEmail(login: String, password: String): User? =
        map[login.trim()]?.let {
            if (it.checkPassword(password))
                it
            else null
        }


    fun clearHolder() {
        map.clear()
    }

    fun registerUserByPhone(
        fullName: String,
        rawPhone: String
    ): User = User.makeUser(fullName, phone = rawPhone)
        .also { user -> addUser(user) }

    fun requestAccessCode(poneNumber: String): String? {
        User.normalizePhone(poneNumber).also { number ->
            if (User.isValidPhone(number)) {
                map.filterKeys { it == number }.values.first() { user ->
                    return user.requestAccessCode()
                }
            }
        }
        return null
    }

    fun loginUser(login: String, pass: String): String? {
        User.normalizePhone(login).also {
            if (User.isValidPhone(it))
                return loginUserByPhone(login, pass)?.userInfo
        }
        return loginUserByEmail(login, pass)?.userInfo
    }
}
