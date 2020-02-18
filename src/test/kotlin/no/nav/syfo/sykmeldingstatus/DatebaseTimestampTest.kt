package no.nav.syfo.sykmeldingstatus

import no.nav.syfo.testutil.TestDB
import no.nav.syfo.util.TimestampUtil
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.sql.Timestamp
import java.time.LocalDateTime

class DatebaseTimestampTest : Spek({

    val db = TestDB()

    describe("Test db") {
        it("Should save timestamp as utc") {
            val timestamp = LocalDateTime.parse("2019-06-02T12:00:01.123")
            val expectedZoneDateTime = TimestampUtil.getAdjustedZonedDateTime(timestamp)
            val sykmeldingStatusEvent = SykmeldingStatusEvent("123", timestamp, StatusEvent.APEN, TimestampUtil.getAdjustedZonedDateTime(timestamp))
            db.registerStatus(sykmeldingStatusEvent)
            val statuser = db.hentSykmeldingStatuser("123")
            statuser.size shouldEqual 1
            statuser.get(0).timestampz shouldEqual expectedZoneDateTime
            statuser.get(0).timestamp shouldEqual timestamp
        }

        it("Should convert and save timestampZ if not provided") {
            val timestamp = LocalDateTime.parse("2019-01-01T12:00:01.1234")
            val expectedZonedTimestamp = TimestampUtil.getAdjustedZonedDateTime(timestamp)
            val sykmeldingStatusEvent = SykmeldingStatusEvent("1234", timestamp, StatusEvent.APEN)
            db.registerStatus(sykmeldingStatusEvent)
            val statuser = db.hentSykmeldingStatuser("1234")
            statuser.size shouldEqual 1
            statuser.get(0).timestamp shouldEqual timestamp
            statuser.get(0).timestampz shouldEqual expectedZonedTimestamp
        }

        it("Should get null when timestamp UTC not saved in database") {
            val timestamp = LocalDateTime.now()
            db.connection.use {
                it.prepareStatement("Insert into sykmeldingstatus(sykmelding_id, event_timestamp, event) values (?, ?, ?)").use {
                    it.setString(1, "12345")
                    it.setTimestamp(2, Timestamp.valueOf(timestamp))
                    it.setString(3, "APEN")
                    it.execute()
                }
                it.commit()
            }
            val statuser = db.hentSykmeldingStatuser("12345")
            statuser.get(0).timestampz shouldEqual null
            statuser.get(0).timestamp shouldEqual timestamp
        }
    }
})
