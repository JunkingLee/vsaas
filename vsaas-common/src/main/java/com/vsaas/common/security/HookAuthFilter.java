package com.vsaas.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class HookAuthFilter implements Filter {

    @Value("${vsaas.hook.secret:}")
    private String hookSecret;

    @Value("${vsaas.hook.auth-enabled:true}")
    private boolean authEnabled = false;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();
        if (path != null && path.startsWith("/api/srs")) {
            if (authEnabled) {
                if (!StringUtils.hasText(hookSecret)) {
                    ((HttpServletResponse) response).sendError(503, "Hook secret not configured");
                    return;
                }
                String provided = req.getHeader("X-Hook-Secret");
                if (provided == null || !provided.equals(hookSecret)) {
                    ((HttpServletResponse) response).sendError(401, "Invalid hook secret");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
