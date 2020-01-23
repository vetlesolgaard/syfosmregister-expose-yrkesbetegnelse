package no.nav.syfo.sykmeldingstatus.kafka.model

import java.time.LocalDateTime

data class KafkaMetadata(
    val sykmeldingId: String,
    val source: String,
    val timestamp: LocalDateTime
)
