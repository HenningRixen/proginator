package com.example.prog1learnapp.config;

import com.example.prog1learnapp.service.lsp.LspPrewarmService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LspLoginSuccessHandlerTest {

    @Test
    void onAuthenticationSuccess_whenPrewarmThrows_stillRedirects() throws Exception {
        LspPrewarmService prewarmService = Mockito.mock(LspPrewarmService.class);
        when(prewarmService.scheduleLoginPrewarm(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new RuntimeException("prewarm-failed"));

        LspLoginSuccessHandler handler = new LspLoginSuccessHandler(prewarmService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login");
        request.setContextPath("");
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "n/a");

        handler.onAuthenticationSuccess(request, response, auth);

        assertEquals(302, response.getStatus());
        assertEquals("/dashboard", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_schedulesPrewarmAndRedirects() throws Exception {
        LspPrewarmService prewarmService = Mockito.mock(LspPrewarmService.class);
        when(prewarmService.scheduleLoginPrewarm(Mockito.eq("alice"), Mockito.anyString())).thenReturn(true);

        LspLoginSuccessHandler handler = new LspLoginSuccessHandler(prewarmService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login");
        request.setContextPath("");
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "n/a");

        handler.onAuthenticationSuccess(request, response, auth);

        verify(prewarmService).scheduleLoginPrewarm(Mockito.eq("alice"), Mockito.anyString());
        assertEquals(302, response.getStatus());
        assertEquals("/dashboard", response.getRedirectedUrl());
    }
}
