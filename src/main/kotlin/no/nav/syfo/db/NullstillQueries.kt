package no.nav.syfo.db

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection

fun Connection.nullstillSykmeldinger(aktorId: String) {
    val log: Logger = LoggerFactory.getLogger("nav.syfo.syfosmregister")
    use { connection ->
        log.info("Nullstiller sykmeldinger på aktor: {}", aktorId)
        connection.autoCommit = false
        connection.prepareStatement("DELETE FROM sykmelding_metadata sm WHERE sm.sykmeldingsid IN (SELECT s.id FROM sykmelding s WHERE pasient_aktoer_id = ?)").use {
            it.setString(1, aktorId)
            it.execute()
        }
        connection.prepareStatement("DELETE FROM sykmelding WHERE pasient_aktoer_id = ?").use {
            it.setString(1, aktorId)
            it.execute()
        }
        connection.commit()
        log.info("Utført: slettet sykmeldinger på aktør: {}", aktorId)
    }
}
