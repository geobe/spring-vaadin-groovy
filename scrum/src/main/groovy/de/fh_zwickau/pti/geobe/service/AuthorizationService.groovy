package de.fh_zwickau.pti.geobe.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * @author georg beier
 */
@Service
class AuthorizationService implements IAuthorizationService{
    @Override
    boolean hasRole(String role) {
        Collection authorities = SecurityContextHolder.context.authentication.authorities
        authorities.any {GrantedAuthority au ->
            au.authority == role
        }
    }

    @Override
    List getRoles() {
        def roles = SecurityContextHolder.context.authentication.authorities*.authority
        return roles
    }

    @Override
    def getUser() {
        return SecurityContextHolder.context.authentication.principal
    }
}
