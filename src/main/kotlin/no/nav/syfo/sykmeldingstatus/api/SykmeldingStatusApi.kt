package no.nav.syfo.sykmeldingstatus.api

import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import no.nav.syfo.aksessering.api.log
import no.nav.syfo.sykmeldingstatus.SykmeldingStatusEvent
import no.nav.syfo.sykmeldingstatus.SykmeldingStatusEventDTO
import no.nav.syfo.sykmeldingstatus.SykmeldingStatusService
import no.nav.syfo.sykmeldingstatus.kafka.model.toSykmeldingStatusKafkaEvent
import no.nav.syfo.sykmeldingstatus.kafka.producer.SykmeldingStatusKafkaProducer

fun Route.registerSykmeldingStatusGETApi(sykmeldingStatusService: SykmeldingStatusService) {
    get("/sykmeldinger/{sykmeldingId}/status") {
        val sykmeldingId = call.parameters["sykmeldingId"]!!
        val principal: JWTPrincipal = call.authentication.principal()!!
        val subject = principal.payload.subject
        val filter = call.request.queryParameters["filter"]
        when (sykmeldingStatusService.erEier(sykmeldingId, subject)) {
            true -> call.respond(toSykmeldingStatusList(sykmeldingStatusService.getSykmeldingStatus(sykmeldingId, filter)))
            else -> call.respond(HttpStatusCode.Forbidden)
        }
    }
}

fun Route.registerSykmeldingStatusApi(sykmeldingStatusService: SykmeldingStatusService, sykmeldingStatusKafkaProducer: SykmeldingStatusKafkaProducer) {
    post("/sykmeldinger/{sykmeldingsid}/status") {
        val sykmeldingId = call.parameters["sykmeldingsid"]!!
        val sykmeldingStatusEventDTO = call.receive<SykmeldingStatusEventDTO>()
        val sykmeldingStatusEvent = SykmeldingStatusEvent(
                sykmeldingId,
                sykmeldingStatusEventDTO.timestamp,
                sykmeldingStatusEventDTO.statusEvent.toStatusEvent())
        try {
            sykmeldingStatusService.registrerStatus(sykmeldingStatusEvent)
            sykmeldingStatusKafkaProducer.send(sykmeldingStatusEventDTO.toSykmeldingStatusKafkaEvent(sykmeldingId))
            call.respond(HttpStatusCode.Created)
        } catch (ex: Exception) {
            log.error("Internal server error", ex)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
