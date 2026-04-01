package br.unifor.healthsys.patient.service;

import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient create(Patient patient) {
        if (patient.getCpf() != null && patientRepository.existsByCpf(patient.getCpf())) {
            throw new IllegalArgumentException("CPF ja cadastrado: " + patient.getCpf());
        }
        return patientRepository.save(patient);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente nao encontrado: " + id));
    }

    public Patient findByCpf(String cpf) {
        return patientRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Paciente nao encontrado com CPF: " + cpf));
    }

    public List<Patient> searchByName(String nome) {
        return patientRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Patient update(Long id, Patient updated) {
        Patient existing = findById(id);
        existing.setNome(updated.getNome());
        existing.setDataNascimento(updated.getDataNascimento());
        existing.setEmail(updated.getEmail());
        existing.setTelefone(updated.getTelefone());
        existing.setSexo(updated.getSexo());
        existing.setTipoSanguineo(updated.getTipoSanguineo());
        existing.setAlergias(updated.getAlergias());
        return patientRepository.save(existing);
    }

    public void delete(Long id) {
        patientRepository.delete(findById(id));
    }
}
