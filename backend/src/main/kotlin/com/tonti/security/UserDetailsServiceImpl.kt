package com.tonti.security

import com.tonti.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findActiveByEmail(email.lowercase().trim())
            ?: throw UsernameNotFoundException("Utilisateur non trouvé avec l'email: $email")

        return UserPrincipal(user)
    }
}
