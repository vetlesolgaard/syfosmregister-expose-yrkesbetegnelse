package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.features.CallId
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.UUID
import no.nav.syfo.Environment
import no.nav.syfo.VaultSecrets
import no.nav.syfo.aksessering.SykmeldingService
import no.nav.syfo.aksessering.api.registerSykmeldingApi
import no.nav.syfo.aksessering.api.registerSykmeldingStatusApi
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.db.DatabaseInterface
import no.nav.syfo.log
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.nullstilling.registerNullstillApi
import no.nav.syfo.rerunkafka.api.registerRerunKafkaApi
import no.nav.syfo.rerunkafka.service.RerunKafkaService

fun createApplicationEngine(
    env: Environment,
    applicationState: ApplicationState,
    database: DatabaseInterface,
    vaultSecrets: VaultSecrets,
    jwkProvider: JwkProvider,
    issuer: String,
    cluster: String,
    rerunKafkaService: RerunKafkaService,
    jwkProviderForRerun: JwkProvider,
    jwkProviderStsOidc: JwkProvider
): ApplicationEngine =
    embeddedServer(Netty, env.applicationPort) {
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        setupAuth(vaultSecrets, jwkProvider, issuer, env, jwkProviderForRerun, jwkProviderStsOidc)
        install(CallId) {
            generate { UUID.randomUUID().toString() }
            verify { callId: String -> callId.isNotEmpty() }
            header(HttpHeaders.XCorrelationId)
        }
        install(StatusPages) {
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

                log.error("Caught exception", cause)
                throw cause
            }
        }

        val sykmeldingService = SykmeldingService(database)
        routing {
            registerNaisApi(applicationState)
            authenticate("jwt") {
                registerSykmeldingApi(sykmeldingService)
            }
            authenticate("rerun") {
                registerRerunKafkaApi(rerunKafkaService)
            }
            authenticate("basic") {
                registerNullstillApi(database, cluster)
            }
            authenticate("oidc") {
                registerSykmeldingStatusApi(sykmeldingService)
            }
        }
        intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
    }