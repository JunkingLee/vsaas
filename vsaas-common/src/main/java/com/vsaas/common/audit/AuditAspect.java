package com.vsaas.common.audit;

import com.vsaas.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Pointcut("within(com.vsaas..controller..*) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void mutatingApis() {}

    @AfterReturning("mutatingApis()")
    public void afterWrite(JoinPoint jp) {
        try {
            String action = jp.getSignature().toShortString();
            Long tenantId = TenantContext.getTenantId();
            Long userId = TenantContext.getUserId();
            String ip = "";
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ip = req.getRemoteAddr();
            }
            log.info("AUDIT tenant={} user={} ip={} action={}", tenantId, userId, ip, action);
        } catch (Exception ignored) {
        }
    }
}
