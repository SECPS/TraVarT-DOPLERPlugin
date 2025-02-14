package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;
import edu.kit.travart.dopler.injection.Injector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Disabled, because this test can only run if the z3 sat solver exists and the tests need a long time. */
@Disabled
class DoplerSamplerTest {

    private final Injector injector = new Injector();
    private final DoplerSampler doplerSampler = injector.getInstance(DoplerSampler.class);
    private final DecisionModelReader decisionModelReader = new DecisionModelReader();

    @Test
    void testNumberOfConfigs1() throws Exception {
        Dopler model = decisionModelReader.read(Path.of("src", "test", "resources", "sampling", "AlwaysTrue.csv"));
        Set<Map<IConfigurable, Boolean>> valid = doplerSampler.sampleValidConfigurations(model);
        Assertions.assertEquals(6, valid.size());
        Set<Map<IConfigurable, Boolean>> invalid = doplerSampler.sampleInvalidConfigurations(model);
        Assertions.assertEquals(122, invalid.size());
    }

    @Test
    void testNumberOfConfigs2() throws Exception {
        Dopler model = decisionModelReader.read(Path.of("src", "test", "resources", "sampling", "DissModel.csv"));
        Set<Map<IConfigurable, Boolean>> valid = doplerSampler.sampleValidConfigurations(model);
        Assertions.assertEquals(288, valid.size());
        Set<Map<IConfigurable, Boolean>> invalid = doplerSampler.sampleInvalidConfigurations(model, 100);
        Assertions.assertEquals(100, invalid.size());
    }

    @Test
    void testNumberOfConfigs3() throws Exception {
        Dopler model = decisionModelReader.read(Path.of("src", "test", "resources", "sampling", "Easy.csv"));
        Set<Map<IConfigurable, Boolean>> valid = doplerSampler.sampleValidConfigurations(model);
        Assertions.assertEquals(3, valid.size());
        Set<Map<IConfigurable, Boolean>> invalid = doplerSampler.sampleInvalidConfigurations(model);
        Assertions.assertEquals(13, invalid.size());
    }
}
