package no.nav.syfo.sykmeldingstatus

import java.time.LocalDateTime

data class SykmeldingSendEvent(
    val id: String,
    val timestamp: LocalDateTime,
    val arbeidsgiver: Arbeidsgiver,
    val sporsmal: Sporsmal
)

data class Arbeidsgiver(
    val sykmeldingId: String,
    val orgnummer: String,
    val juridiskOrgnummer: String?,
    val orgnavn: String
)

data class Sporsmal(
    val tekst: String,
    val shortName: ShortName,
    val svar: Svar
)

data class Svar(
    val sykmeldingId: String,
    val sporsmalId: Int?,
    val svartype: Svartype,
    val svar: String
)

enum class ShortName {
    ARBEIDSSITUASJON, NY_NARMESTE_LEDER, FRAVAER, PERIODE, FORSIKRING
}

enum class Svartype {
    ARBEIDSSITUASJON,
    PERIODE,
    PERIODER,
    JA_NEI
}
