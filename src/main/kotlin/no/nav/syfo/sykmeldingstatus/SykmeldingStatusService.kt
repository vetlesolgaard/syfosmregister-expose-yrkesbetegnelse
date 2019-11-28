package no.nav.syfo.sykmeldingstatus

import no.nav.syfo.db.DatabaseInterface
import no.nav.syfo.log

class SykmeldingStatusService(private val database: DatabaseInterface) {

    fun registrerStatus(sykmeldingStatusEvent: SykmeldingStatusEvent) {
        database.registerStatus(sykmeldingStatusEvent)
    }

    fun registrerSendt(sykmeldingSendEvent: SykmeldingSendEvent) {
        val sykmeldingStatusEvent = SykmeldingStatusEvent(sykmeldingSendEvent.sykmeldingId, sykmeldingSendEvent.timestamp, StatusEvent.SENDT)
        registrerStatus(sykmeldingStatusEvent)
        slettGamleSvarHvisFinnesFraFor(sykmeldingSendEvent.sykmeldingId)
        database.lagreArbeidsgiver(sykmeldingSendEvent)
        database.lagreSporsmalOgSvar(sykmeldingSendEvent.sporsmal)
    }

    fun registrerBekreftet(sykmeldingBekreftEvent: SykmeldingBekreftEvent) {
        val sykmeldingStatusEvent = SykmeldingStatusEvent(sykmeldingBekreftEvent.sykmeldingId, sykmeldingBekreftEvent.timestamp, StatusEvent.BEKREFTET)
        registrerStatus(sykmeldingStatusEvent)
        slettGamleSvarHvisFinnesFraFor(sykmeldingBekreftEvent.sykmeldingId)
        sykmeldingBekreftEvent.sporsmal?.forEach {
            database.lagreSporsmalOgSvar(it)
        }
    }

    private fun slettGamleSvarHvisFinnesFraFor(sykmeldingId: String) {
        val svarFinnesFraFor = database.svarFinnesFraFor(sykmeldingId)
        if (svarFinnesFraFor) {
            log.info("Sletter tidligere svar for sykmelding {}", sykmeldingId)
            database.slettArbeidsgiver(sykmeldingId)
            database.slettSvar(sykmeldingId)
        }
    }
}
