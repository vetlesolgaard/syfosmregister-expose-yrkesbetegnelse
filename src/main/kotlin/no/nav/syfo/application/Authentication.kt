package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.Environment
import no.nav.syfo.VaultSecrets
import no.nav.syfo.log

fun Application.setupAuth(vaultSecrets: VaultSecrets, jwkProvider: JwkProvider, issuer: String, env: Environment, jwkProviderForRerun: JwkProvider, stsOidcJwkProvider: JwkProvider) {
    install(Authentication) {
        jwt(name = "jwt") {
            verifier(jwkProvider, issuer)
            validate { credentials ->
                when {
                    hasLoginserviceClientIdAudience(credentials, vaultSecrets) -> JWTPrincipal(credentials.payload)
                    else -> unauthorized(credentials)
                }
            }
        }
        jwt(name = "rerun") {
            verifier(jwkProviderForRerun, env.jwtIssuer)
            validate { credentials ->
                when {
                    hasValidSystemToken(credentials, env) -> JWTPrincipal(credentials.payload)
                    else -> unauthorized(credentials)
                }
            }
        }
        basic(name = "basic") {
            validate { credentials ->
                if (credentials.name == vaultSecrets.syfomockUsername && credentials.password == vaultSecrets.syfomockPassword) {
                    UserIdPrincipal(credentials.name)
                } else null
            }
        }

        jwt(name = "oidc") {
            verifier(stsOidcJwkProvider, env.stsOidcIssuer)
            validate { credentials ->
                when {
                    isValidStsOidcToken(credentials, env) -> JWTPrincipal(credentials.payload)
                    else -> unauthorized(credentials)
                }
            }
        }
    }
}

fun isValidStsOidcToken(credentials: JWTCredential, env: Environment): Boolean {
    return credentials.payload.audience.contains(env.stsOidcAudience) &&
            "srvsyfoservice".equals(credentials.payload.getClaim("sub").asString())
}

fun unauthorized(credentials: JWTCredential): Principal? {
    log.warn(
            "Auth: Unexpected audience for jwt {}, {}",
            StructuredArguments.keyValue("issuer", credentials.payload.issuer),
            StructuredArguments.keyValue("audience", credentials.payload.audience)
    )
    return null
}

fun hasLoginserviceClientIdAudience(credentials: JWTCredential, vaultSecrets: VaultSecrets): Boolean {
    return credentials.payload.audience.contains(vaultSecrets.loginserviceClientId)
}

fun hasValidSystemToken(credentials: JWTCredential, env: Environment): Boolean {
    val appId: String = credentials.payload.getClaim("azp").asString()
    log.info("authorization attempt for $appId")
    if (appId in env.appIds && env.clientId in credentials.payload.audience) {
        log.info("authorization ok")
        return true
    }
    return false
}