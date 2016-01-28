package de.fh_zwickau.pti.geobe.service

import org.springframework.stereotype.Service

/**
 * provide a simple interface to determine identity, role and other
 * authorisation properties from the security implementation
 *
 * @author georg beier
 */
interface IAuthorizationService {
    boolean hasRole(String role)
    List getRoles()
    def getUser()
}