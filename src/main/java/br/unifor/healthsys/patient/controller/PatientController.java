package br.unifor.healthsys.patient.controller;

import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(patient));
    }

    @GetMapping
    public ResponseEntity<List<Patient>> findAll(@RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(patientService.findAll(ativo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Patient> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(patientService.findByCpf(cpf));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(@RequestParam String nome) {
        return ResponseEntity.ok(patientService.searchByName(nome));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(id, patient));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Patient> updateStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(patientService.updateStatus(id, ativo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
