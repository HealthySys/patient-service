package br.unifor.healthsys.patient.repository;

import br.unifor.healthsys.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Patient> findByNomeContainingIgnoreCase(String nome);
}
