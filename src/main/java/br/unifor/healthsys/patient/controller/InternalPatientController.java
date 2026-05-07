package br.unifor.healthsys.patient.controller;

import br.unifor.healthsys.patient.dto.AllergyInput;
import br.unifor.healthsys.patient.dto.InternalPatientSummaryResponse;
import br.unifor.healthsys.patient.dto.VaccineInput;
import br.unifor.healthsys.patient.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return ResponseEntity.ok(patientService.findSummaryById(id));
    }

    @PostMapping("/{id}/allergies")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<Void> addAllergies(@PathVariable Long id,
                                             @RequestBody List<AllergyInput> allergies) {
        patientService.addAllergies(id, allergies);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/vaccines")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<Void> addVaccines(@PathVariable Long id,
                                            @RequestBody List<VaccineInput> vaccines) {
        patientService.addVaccines(id, vaccines);
        return ResponseEntity.noContent().build();
    }
}
