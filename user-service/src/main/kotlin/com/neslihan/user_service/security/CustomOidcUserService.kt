package com.neslihan.user_service.security

import com.neslihan.user_service.model.User
import com.neslihan.user_service.repository.UserRepository
import com.neslihan.user_service.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CustomOidcUserService(
    private val userService: UserService
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val delegate = OidcUserService()
        val oidcUser = delegate.loadUser(userRequest)

        val email = oidcUser.getAttribute<String>("email")
            ?: throw OAuth2AuthenticationException(OAuth2Error("email_not_found"), "Email not found in OIDC response")

        var user = userService.findByEmail(email)
        if (user == null) {
            user = User(
                username = email,
                email = email,
                password = UUID.randomUUID().toString() // unusable password
            )
            user = userService.registerUser(user)
        }
        return oidcUser
    }
}
