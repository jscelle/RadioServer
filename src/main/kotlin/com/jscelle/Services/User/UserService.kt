import com.jscelle.Services.User.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

class UserService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS application.Users (name VARCHAR(255) PRIMARY KEY, password VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL);"
        private const val INSERT_USER =
            "INSERT INTO application.Users (name, password, status) VALUES (?, ?, ?);"
        private const val SELECT_USER_BY_NAME_AND_PASSWORD =
            "SELECT name, status FROM application.Users WHERE name = ? AND password = ?;"
        private const val SELECT_USER_BY_NAME =
            "SELECT name, status FROM application.Users WHERE name = ?;"
        private const val UPDATE_USER_PROFILE =
            "UPDATE application.Users SET password = ?, status = ? WHERE name = ?;"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_USERS)
    }

    // Function to sign up a new user with status validation
    suspend fun signUp(name: String, password: String, status: String): User = withContext(Dispatchers.IO) {
        validateStatus(status)

        val statement = connection.prepareStatement(INSERT_USER)
        statement.setString(1, name)
        statement.setString(2, password)
        statement.setString(3, status)
        statement.executeUpdate()

        return@withContext User(name, password, status)
    }

    // Function to sign in a user
    suspend fun signIn(name: String, password: String): User = withContext(Dispatchers.IO) {

        val statement = connection.prepareStatement(SELECT_USER_BY_NAME_AND_PASSWORD)
        statement.setString(1, name)
        statement.setString(2, password)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            return@withContext User(resultSet.getString("name"), "", resultSet.getString("status"))
        } else {
            throw NotFoundException("User not found or invalid credentials.")
        }
    }

    // Function to get user by name
    suspend fun getUserByName(name: String): User = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_USER_BY_NAME)
        statement.setString(1, name)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val status = resultSet.getString("status")
            return@withContext User(name, "", status) // Omitting password for security
        } else {
            throw NotFoundException("User not found")
        }
    }

    // Function to update user profile
    suspend fun updateProfile(user: User) = withContext(Dispatchers.IO) {
        validateStatus(user.status)

        // Assuming you have an UPDATE query to update the user's profile in the Users table
        val updateStatement = connection.prepareStatement(UPDATE_USER_PROFILE)
        updateStatement.setString(1, user.password)
        updateStatement.setString(2, user.status)
        updateStatement.setString(3, user.name)
        val rowsUpdated = updateStatement.executeUpdate()

        if (rowsUpdated == 0) {
            throw NotFoundException("User not found.")
        }
    }

    private fun validateStatus(status: String) {
        // Implement status validation logic, e.g., check if it's a valid user status
        if (status !in listOf("user", "seller", "admin")) {
            throw BadRequestException("Invalid user status.")
        }
    }

    // Exception classes for better error handling
    class NotFoundException(message: String) : Exception(message)
    class BadRequestException(message: String) : Exception(message)
}
