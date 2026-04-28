package br.unifor.healthsys.patient.dto;

import java.time.LocalDate;

public record VaccineInput(
        String nomeVacina,
        LocalDate dataAplicacao,
        String lote,
        String profissionalResp
) {
}
