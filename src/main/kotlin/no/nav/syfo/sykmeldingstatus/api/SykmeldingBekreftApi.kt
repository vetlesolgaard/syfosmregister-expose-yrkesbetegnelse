package no.nav.syfo.sykmeldingstatus.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.syfo.aksessering.api.log
import no.nav.syfo.sykmeldingstatus.SykmeldingStatusService
import no.nav.syfo.sykmeldingstatus.kafka.model.toSykmeldingStatusKafkaEvent
import no.nav.syfo.sykmeldingstatus.kafka.producer.SykmeldingStatusKafkaProducer

fun Route.registerSykmeldingBekreftApi(sykmeldingStatusService: SykmeldingStatusService, sykmeldingStatusKafkaProducer: SykmeldingStatusKafkaProducer) {

    post("/sykmeldinger/{sykmeldingid}/bekreft") {
        val sykmeldingId = call.parameters["sykmeldingid"]!!
        val sykmeldingBekreftEventDTO = call.receive<SykmeldingBekreftEventDTO>()

        try {
            sykmeldingStatusService.registrerBekreftet(tilSykmeldingBekreftEvent(sykmeldingId, sykmeldingBekreftEventDTO))
            log.info("Bekreftet sykmelding {}", sykmeldingId)
            sykmeldingStatusKafkaProducer.send(sykmeldingBekreftEventDTO.toSykmeldingStatusKafkaEvent(sykmeldingId))
            call.respond(HttpStatusCode.Created)
        } catch (ex: Exception) {
            log.error("Noe gikk galt ved bekrefting av sykmelding {}", sykmeldingId, ex)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}