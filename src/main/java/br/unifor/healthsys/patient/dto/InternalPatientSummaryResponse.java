package br.unifor.healthsys.patient.dto;

public record InternalPatientSummaryResponse(
        Long id,
        String nome,
        boolean ativo,
        String email
) {
}
