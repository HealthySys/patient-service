package br.unifor.healthsys.patient.audit;

import br.unifor.healthsys.patient.model.Patient;
import br.unifor.healthsys.patient.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(br.unifor.healthsys.patient.audit.Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Audited audited = signature.getMethod().getAnnotation(Audited.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser principal = authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
                ? user
                : null;

        String ipAddress = "unknown";
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getRemoteAddr();
        }

        auditLogRepository.save(AuditLog.builder()
                .userId(principal != null ? principal.userId() : null)
                .action(audited.action())
                .resource(audited.resource())
                .resourceId(resolveResourceId(result, joinPoint.getArgs()))
                .timestampUtc(LocalDateTime.now(ZoneOffset.UTC))
                .ipAddress(ipAddress)
                .build());

        return result;
    }

    private String resolveResourceId(Object result, Object[] args) {
        if (result instanceof Patient patientResult) {
            if (patientResult.getId() != null) {
                return String.valueOf(patientResult.getId());
            }
            if (patientResult.getCpf() != null) {
                return truncate(patientResult.getCpf());
            }
        }

        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof Number || arg instanceof CharSequence || arg instanceof java.util.UUID) {
                return truncate(String.valueOf(arg));
            }
            if (arg instanceof Patient patientArg) {
                if (patientArg.getId() != null) {
                    return String.valueOf(patientArg.getId());
                }
                if (patientArg.getCpf() != null) {
                    return truncate(patientArg.getCpf());
                }
                if (patientArg.getNome() != null) {
                    return truncate(patientArg.getNome());
                }
            }
        }

        return null;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 100 ? value : value.substring(0, 100);
    }
}
