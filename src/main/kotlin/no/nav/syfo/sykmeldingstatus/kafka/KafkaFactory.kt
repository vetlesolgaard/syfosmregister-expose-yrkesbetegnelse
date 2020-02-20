package no.nav.syfo.sykmeldingstatus.kafka

import java.util.Properties
import no.nav.syfo.Environment
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.sykmeldingstatus.kafka.model.SykmeldingStatusKafkaMessage
import no.nav.syfo.sykmeldingstatus.kafka.producer.SykmeldingStatusBackupKafkaProducer
import no.nav.syfo.sykmeldingstatus.kafka.util.JacksonKafkaSerializer
import org.apache.kafka.clients.producer.KafkaProducer

class KafkaFactory private constructor() {
    companion object {
        fun getSykmeldingStatusKafkaProducer(kafkaBaseConfig: Properties, environment: Environment): SykmeldingStatusBackupKafkaProducer {
            val kafkaStatusProducerConfig = kafkaBaseConfig.toProducerConfig(
                    "${environment.applicationName}-producer", JacksonKafkaSerializer::class
            )
            val kafkaProducer = KafkaProducer<String, SykmeldingStatusKafkaMessage>(kafkaStatusProducerConfig)
            return SykmeldingStatusBackupKafkaProducer(kafkaProducer, environment.sykmeldingStatusBackupTopic)
        }
    }
}
