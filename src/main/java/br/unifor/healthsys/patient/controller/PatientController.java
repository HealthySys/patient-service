package br.unifor.healthsys.patient.controller;

import br.unifor.healthsys.patient.audit.Audited;
import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Pacientes", description = "Cadastro, consulta e manutenção de pacientes")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um paciente", description = "Perfis: RECEPCIONISTA, ENFERMEIRO, ADMIN.")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ENFERMEIRO','ADMIN')")
    @Audited(action = "CREATE", resource = "PATIENT")
    public ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(patient));
    }

    @GetMapping
    @Operation(summary = "Lista pacientes", description = "Opcionalmente filtra por status (ativo=true/false).")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "READ_ALL", resource = "PATIENT")
    public ResponseEntity<List<Patient>> findAll(@RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(patientService.findAll(ativo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca paciente por ID")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "READ", resource = "PATIENT")
    public ResponseEntity<Patient> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Busca paciente por CPF")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "READ_BY_CPF", resource = "PATIENT")
    public ResponseEntity<Patient> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(patientService.findByCpf(cpf));
    }

    @GetMapping("/search")
    @Operation(summary = "Busca pacientes por nome", description = "Pesquisa parcial pelo nome do paciente.")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','RECEPCIONISTA','ADMIN')")
    @Audited(action = "SEARCH", resource = "PATIENT")
    public ResponseEntity<List<Patient>> search(@RequestParam String nome) {
        return ResponseEntity.ok(patientService.searchByName(nome));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um paciente", description = "Perfis: ADMIN, ENFERMEIRO, MEDICO.")
    @PreAuthorize("hasAnyRole('ADMIN','ENFERMEIRO','MEDICO')")
    @Audited(action = "UPDATE", resource = "PATIENT")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(id, patient));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ativa/inativa um paciente")
    @PreAuthorize("hasAnyRole('ADMIN','ENFERMEIRO','MEDICO')")
    @Audited(action = "UPDATE_STATUS", resource = "PATIENT")
    public ResponseEntity<Patient> updateStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        return ResponseEntity.ok(patientService.updateStatus(id, ativo));
    }

}
