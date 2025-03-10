package com.neslihan.user_service.service

import com.neslihan.user_service.model.User
import com.neslihan.user_service.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {
    fun registerUser(user: User): User {
        val hashedPassword = passwordEncoder.encode(user.password)
        val userToSave = user.copy(password = hashedPassword)
        return userRepository.save(userToSave)
    }
    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun validatePassword(rawPassword: String, hashedPassword: String): Boolean = passwordEncoder.matches(rawPassword, hashedPassword)
}