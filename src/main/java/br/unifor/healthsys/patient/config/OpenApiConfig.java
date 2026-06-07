package br.unifor.healthsys.patient.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HealthSys - Patient Service API",
                version = "1.0.0",
                description = """
                        Serviço de cadastro e gestão de pacientes da plataforma HealthSys.

                        Responsabilidades:
                        - CRUD de pacientes e busca por CPF/nome (`/api/patients`)
                        - Ativação/inativação de cadastros
                        - Registro de alergias e vacinas via API interna (`/api/internal/patients`)

                        Requer token JWT emitido pelo user-service.""",
                contact = @Contact(name = "HealthSys - UNIFOR", email = "healthsys@unifor.br"),
                license = @License(name = "Uso acadêmico/interno")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtido em POST /api/auth/login (user-service). Informe apenas o token (sem o prefixo 'Bearer')."
)
public class OpenApiConfig {
}
