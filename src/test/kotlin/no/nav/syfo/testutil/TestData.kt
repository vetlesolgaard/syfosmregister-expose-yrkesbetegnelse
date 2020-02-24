package no.nav.syfo.testutil

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.VaultSecrets
import no.nav.syfo.aksessering.api.PeriodetypeDTO
import no.nav.syfo.sykmelding.internal.model.AdresseDTO
import no.nav.syfo.sykmelding.internal.model.AnnenFraversArsakDTO
import no.nav.syfo.sykmelding.internal.model.BehandlerDTO
import no.nav.syfo.sykmelding.internal.model.BehandlingsutfallDTO
import no.nav.syfo.sykmelding.internal.model.DiagnoseDTO
import no.nav.syfo.sykmelding.internal.model.InternalSykmeldingDTO
import no.nav.syfo.sykmelding.internal.model.KontaktMedPasientDTO
import no.nav.syfo.sykmelding.internal.model.MedisinskVurderingDTO
import no.nav.syfo.sykmelding.internal.model.RegelStatusDTO
import no.nav.syfo.sykmelding.internal.model.SykmeldingsperiodeDTO

fun getVaultSecrets(): VaultSecrets {
    return VaultSecrets(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "")
}

fun getInternalSykmelding(skjermet: Boolean = false): InternalSykmeldingDTO {
    return InternalSykmeldingDTO(
            id = "1",
            utdypendeOpplysninger = emptyMap(),
            kontaktMedPasient = KontaktMedPasientDTO(null, null),
            sykmeldingsperioder = getPerioder(),
            sykmeldingStatus = no.nav.syfo.sykmelding.internal.model.SykmeldingStatusDTO("APEN", OffsetDateTime.now(ZoneOffset.UTC), null, emptyList()),
            behandlingsutfall = BehandlingsutfallDTO(RegelStatusDTO.OK, emptyList()),
            medisinskVurdering = getMedisinskVurdering(),
            behandler = BehandlerDTO(
                    "fornavn", null, "etternavn",
                    "123", "01234567891", null, null,
                    AdresseDTO(null, null, null, null, null), null),
            behandletTidspunkt = OffsetDateTime.now(ZoneOffset.UTC),
            mottattTidspunkt = OffsetDateTime.now(ZoneOffset.UTC),
            skjermesForPasient = false,
            meldingTilNAV = null,
            prognose = null,
            arbeidsgiver = null,
            tiltakNAV = null,
            syketilfelleStartDato = null,
            tiltakArbeidsplassen = null,
            navnFastlege = null,
            meldingTilArbeidsgiver = null,
            legekontorOrgnummer = null,
            andreTiltak = null
    )
}

fun getMedisinskVurdering(): MedisinskVurderingDTO {
    return MedisinskVurderingDTO(
            hovedDiagnose = DiagnoseDTO("1", "system", "hoveddiagnose"),
            biDiagnoser = listOf(DiagnoseDTO("2", "system2", "bidagnose")),
            annenFraversArsak = AnnenFraversArsakDTO("", emptyList()),
            svangerskap = false,
            yrkesskade = false,
            yrkesskadeDato = null
    )
}

fun getPerioder(): List<SykmeldingsperiodeDTO> {
    return listOf(SykmeldingsperiodeDTO(LocalDate.now(), LocalDate.now(), null, null, null, PeriodetypeDTO.AKTIVITET_IKKE_MULIG, null, false))
}