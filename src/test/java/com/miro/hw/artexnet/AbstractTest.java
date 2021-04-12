package com.miro.hw.artexnet;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.jeasy.random.FieldPredicates.ofType;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractTest {
    protected static Random random = new Random();
    protected static EasyRandom objectGenerator;

    /**
     * Initializes a new instance of the class.
     */
    public AbstractTest() {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .excludeField(ofType(Long.class))
                .objectPoolSize(100)
                .randomizationDepth(3)
                .charset(StandardCharsets.UTF_8)
                .stringLengthRange(5, 20)
                .collectionSizeRange(1, 10)
                .scanClasspathForConcreteTypes(true)
                .overrideDefaultInitialization(false)
                .ignoreRandomizationErrors(true);

        objectGenerator = new EasyRandom(parameters);
    }

    public static int getRandomNumber() {
        return getRandomNumber(100);
    }

    public static int getRandomNumber(int limit) {
        return 1 + random.nextInt(limit);
    }

}