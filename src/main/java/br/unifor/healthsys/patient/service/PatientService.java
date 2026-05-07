package br.unifor.healthsys.patient.service;

import br.unifor.healthsys.patient.dto.AllergyInput;
import br.unifor.healthsys.patient.dto.InternalPatientSummaryResponse;
import br.unifor.healthsys.patient.dto.VaccineInput;
import br.unifor.healthsys.patient.model.Allergy;
import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.model.Vaccine;
import br.unifor.healthsys.patient.exception.ConflictException;
import br.unifor.healthsys.patient.exception.NotFoundException;
import br.unifor.healthsys.patient.repository.PatientRepository;
import br.unifor.healthsys.patient.security.AuthenticatedUser;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "patient", allEntries = true),
            @CacheEvict(value = "patient-summary", allEntries = true)
    })
    public Patient create(Patient patient) {
        validateCpf(patient.getCpf(), null);
        associateChildren(patient);
        return patientRepository.save(patient);
    }

    public List<Patient> findAll(Boolean ativo) {
        if (ativo == null) {
            return patientRepository.findAll();
        }

        return patientRepository.findByAtivo(ativo);
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paciente nao encontrado: " + id));
    }

    @Cacheable(value = "patient-summary", key = "#id")
    @Transactional(readOnly = true)
    public InternalPatientSummaryResponse findSummaryById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paciente nao encontrado: " + id));
        return new InternalPatientSummaryResponse(
                patient.getId(),
                patient.getNome(),
                patient.isAtivo(),
                patient.getEmail()
        );
    }

    public Patient findByCpf(String cpf) {
        return patientRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundException("Paciente nao encontrado com CPF: " + cpf));
    }

    public Patient findByEmail(String email) {
        return patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Paciente nao encontrado para o usuario autenticado."));
    }

    public List<Patient> searchByName(String nome) {
        return patientRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "patient", key = "#id"),
            @CacheEvict(value = "patient-summary", key = "#id")
    })
    public Patient update(Long id, Patient updated) {
        Patient existing = findById(id);
        validateCpf(updated.getCpf(), id);

        existing.setNome(updated.getNome());
        existing.setDataNascimento(updated.getDataNascimento());
        existing.setCpf(updated.getCpf());
        existing.setEmail(updated.getEmail());
        existing.setTelefone(updated.getTelefone());
        existing.setSexo(updated.getSexo());
        existing.setEndereco(updated.getEndereco());
        existing.setTipoSanguineo(updated.getTipoSanguineo());
        existing.setAtivo(updated.isAtivo());

        existing.getAlergias().clear();
        for (Allergy a : updated.getAlergias()) {
            a.setPatient(existing);
            existing.getAlergias().add(a);
        }

        existing.getVacinas().clear();
        for (Vaccine v : updated.getVacinas()) {
            v.setPatient(existing);
            existing.getVacinas().add(v);
        }

        return patientRepository.save(existing);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "patient", key = "#id"),
            @CacheEvict(value = "patient-summary", key = "#id")
    })
    public Patient updateStatus(Long id, boolean ativo) {
        Patient existing = findById(id);
        existing.setAtivo(ativo);
        return patientRepository.save(existing);
    }

    @Caching(evict = {
            @CacheEvict(value = "patient", key = "#id"),
            @CacheEvict(value = "patient-summary", key = "#id")
    })
    public void delete(Long id) {
        patientRepository.delete(findById(id));
    }

    @Transactional
    @CacheEvict(value = "patient", key = "#patientId")
    public Patient addAllergies(Long patientId, List<AllergyInput> inputs) {
        Patient patient = findById(patientId);
        if (inputs == null || inputs.isEmpty()) {
            return patient;
        }
        for (AllergyInput input : inputs) {
            if (input == null || input.nomeAlergia() == null || input.nomeAlergia().isBlank()) {
                continue;
            }
            Allergy allergy = Allergy.builder()
                    .nomeAlergia(input.nomeAlergia())
                    .severidade(input.severidade())
                    .patient(patient)
                    .build();
            patient.getAlergias().add(allergy);
        }
        return patientRepository.save(patient);
    }

    @Transactional
    @CacheEvict(value = "patient", key = "#patientId")
    public Patient addVaccines(Long patientId, List<VaccineInput> inputs) {
        Patient patient = findById(patientId);
        if (inputs == null || inputs.isEmpty()) {
            return patient;
        }
        for (VaccineInput input : inputs) {
            if (input == null || input.nomeVacina() == null || input.nomeVacina().isBlank()) {
                continue;
            }
            Vaccine vaccine = Vaccine.builder()
                    .nomeVacina(input.nomeVacina())
                    .dataAplicacao(input.dataAplicacao())
                    .lote(input.lote())
                    .profissionalResp(input.profissionalResp())
                    .patient(patient)
                    .build();
            patient.getVacinas().add(vaccine);
        }
        return patientRepository.save(patient);
    }

    public Patient findOwnPatient(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.email() == null || authenticatedUser.email().isBlank()) {
            throw new AccessDeniedException("Sessao sem e-mail valido para localizar o paciente.");
        }

        return findByEmail(authenticatedUser.email());
    }

    public Patient findAuthorizedById(Long id, AuthenticatedUser authenticatedUser) {
        Patient patient = findById(id);
        ensureOwnPatientAccess(patient, authenticatedUser);
        return patient;
    }

    public void ensureOwnPatientAccess(Patient patient, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.email() == null || authenticatedUser.email().isBlank()) {
            throw new AccessDeniedException("Sessao sem e-mail valido para validar acesso ao paciente.");
        }

        if (patient.getEmail() == null || !patient.getEmail().equalsIgnoreCase(authenticatedUser.email())) {
            throw new AccessDeniedException("Paciente pode visualizar apenas o proprio cadastro.");
        }
    }

    private void associateChildren(Patient patient) {
        if (patient.getAlergias() != null) {
            patient.getAlergias().forEach(a -> a.setPatient(patient));
        }
        if (patient.getVacinas() != null) {
            patient.getVacinas().forEach(v -> v.setPatient(patient));
        }
    }

    private void validateCpf(String cpf, Long currentPatientId) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatorio para cadastro de paciente.");
        }

        patientRepository.findByCpf(cpf)
                .filter(patient -> !Objects.equals(patient.getId(), currentPatientId))
                .ifPresent(patient -> {
                    throw new ConflictException("CPF já cadastrado: " + cpf);
                });
    }
}
