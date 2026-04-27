package br.unifor.healthsys.patient.controller;

import br.unifor.healthsys.patient.dto.InternalPatientSummaryResponse;
import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/patients")
public class InternalPatientController {

    private final PatientService patientService;

    public InternalPatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/{id}/summary")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<InternalPatientSummaryResponse> findSummary(@PathVariable Long id) {
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(new InternalPatientSummaryResponse(
                patient.getId(),
                patient.getNome(),
                patient.isAtivo(),
                patient.getEmail()
        ));
    }
}
