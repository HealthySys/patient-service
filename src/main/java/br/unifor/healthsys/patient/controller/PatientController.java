package br.unifor.healthsys.patient.controller;

import br.unifor.healthsys.patient.audit.Audited;
import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.security.AuthenticatedUser;
import br.unifor.healthsys.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ENFERMEIRO','ADMIN')")
    @Audited(action = "CREATE", resource = "PATIENT")
    public ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(patient));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "READ_ALL", resource = "PATIENT")
    public ResponseEntity<List<Patient>> findAll(@RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(patientService.findAll(ativo));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    @Audited(action = "READ_SELF", resource = "PATIENT")
    public ResponseEntity<Patient> findCurrentPatient(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(patientService.findOwnPatient(authenticatedUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN','PACIENTE')")
    @Audited(action = "READ", resource = "PATIENT")
    public ResponseEntity<Patient> findById(@PathVariable Long id,
                                            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser != null && authenticatedUser.hasRole("PACIENTE")) {
            return ResponseEntity.ok(patientService.findAuthorizedById(id, authenticatedUser));
        }

        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/cpf/{cpf}")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "READ_BY_CPF", resource = "PATIENT")
    public ResponseEntity<Patient> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(patientService.findByCpf(cpf));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "SEARCH", resource = "PATIENT")
    public ResponseEntity<List<Patient>> search(@RequestParam String nome) {
        return ResponseEntity.ok(patientService.searchByName(nome));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ENFERMEIRO','MEDICO')")
    @Audited(action = "UPDATE", resource = "PATIENT")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(id, patient));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ENFERMEIRO','MEDICO')")
    @Audited(action = "UPDATE_STATUS", resource = "PATIENT")
    public ResponseEntity<Patient> updateStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(patientService.updateStatus(id, ativo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ENFERMEIRO','MEDICO')")
    @Audited(action = "DELETE", resource = "PATIENT")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
