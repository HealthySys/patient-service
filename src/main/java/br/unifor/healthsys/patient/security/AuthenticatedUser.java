package br.unifor.healthsys.patient.security;

public record AuthenticatedUser(
        Long userId,
        String username,
        String role,
        String email,
        String nome
) {
    public boolean hasRole(String expectedRole) {
        return role != null && role.equalsIgnoreCase(expectedRole);
    }
}
