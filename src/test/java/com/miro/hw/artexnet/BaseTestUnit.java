package com.miro.hw.artexnet;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Ignore
public class BaseTestUnit extends AbstractTest {

    protected static Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // setup derived classes mocks
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() { }

}
