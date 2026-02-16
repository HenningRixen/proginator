package com.example.prog1learnapp.config;

import com.example.prog1learnapp.service.lsp.LspPrewarmService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LspLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(LspLoginSuccessHandler.class);

    private final LspPrewarmService lspPrewarmService;

    public LspLoginSuccessHandler(LspPrewarmService lspPrewarmService) {
        this.lspPrewarmService = lspPrewarmService;
        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        long startedNs = System.nanoTime();
        boolean prewarmScheduled = false;

        try {
            HttpSession session = request.getSession(false);
            String principalName = authentication != null ? authentication.getName() : null;
            String httpSessionId = session != null ? session.getId() : null;
            prewarmScheduled = lspPrewarmService.scheduleLoginPrewarm(principalName, httpSessionId);
        } catch (RuntimeException e) {
            log.warn("LSP prewarm schedule failed during login error={}", e.getMessage());
        }

        log.info("Login success principal={} prewarmScheduled={} loginHookDurationMs={}",
                authentication != null ? authentication.getName() : "unknown",
                prewarmScheduled,
                elapsedMs(startedNs));
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private long elapsedMs(long startedAtNs) {
        return (System.nanoTime() - startedAtNs) / 1_000_000;
    }
}
