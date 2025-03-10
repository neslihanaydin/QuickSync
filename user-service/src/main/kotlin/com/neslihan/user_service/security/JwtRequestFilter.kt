package com.neslihan.user_service.security

import com.neslihan.user_service.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtRequestFilter(
    private val jwtTokenValidator: JwtTokenValidator,
    private val userService: UserService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
         try {
             val authHeader = request.getHeader("Authorization")
             if (authHeader != null && authHeader.startsWith("Bearer ")) {
                 val jwt = authHeader.substring(7)
                 val username = jwtTokenValidator.extractUsername(jwt)
                 if (username != null && SecurityContextHolder.getContext().authentication == null) {
                     val user = userService.findByUsername(username)
                     if (user != null && jwtTokenValidator.validateToken(jwt, user)) {
                         val authToken = UsernamePasswordAuthenticationToken(user, null, emptyList())
                         authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                         SecurityContextHolder.getContext().authentication = authToken
                     }
                 }
             }
         } catch (e: Exception) {
             logger.error("JWT Authentication failed: ${e.message}")
         }
        filterChain.doFilter(request, response)
    }
}