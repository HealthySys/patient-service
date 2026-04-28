package br.unifor.healthsys.patient.dto;

import br.unifor.healthsys.patient.model.Allergy;

public record AllergyInput(
        String nomeAlergia,
        Allergy.Severidade severidade
) {
}
