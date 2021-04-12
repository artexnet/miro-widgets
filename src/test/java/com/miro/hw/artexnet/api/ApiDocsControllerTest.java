package com.miro.hw.artexnet.api;

import com.miro.hw.artexnet.AbstractTest;
import com.miro.hw.artexnet.BaseTestUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiDocsControllerTest extends BaseTestUnit {
    private static final String SWAGGER_URI = "swagger-ui.html";

    @InjectMocks
    private ApiDocsController apiDocsController;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        super.setUp();
        mockMvc = MockMvcBuilders.standaloneSetup(apiDocsController).alwaysDo(print()).build();
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    @Test
    public void getDocs() throws Exception {
        MvcResult result = mockMvc.perform(get("/api-docs"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        ModelAndView mv = result.getModelAndView();

        assertNotNull(mv);
        assertTrue(mv.toString().contains(SWAGGER_URI));
    }

}
