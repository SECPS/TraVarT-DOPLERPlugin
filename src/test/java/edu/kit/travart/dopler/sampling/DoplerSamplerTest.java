package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.io.DecisionModelReader;
import edu.kit.dopler.model.Dopler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

class DoplerSamplerTest {

    private final DoplerSampler doplerSampler = new DoplerSampler();
    private final DecisionModelReader decisionModelReader = new DecisionModelReader();

    @Disabled
    @Test
    void testNumberOfConfigs() throws Exception {
        Dopler model = decisionModelReader.read(Path.of("src/test/resources/sampling/AlwaysTrue.csv"));
        Set<Map<IConfigurable, Boolean>> configurations = doplerSampler.sampleValidConfigurations(model);
        Assertions.assertEquals(6, configurations.size());
    }

    @Disabled
    @Test
    void testNumberOfConfigs2() throws Exception {
        Dopler model = decisionModelReader.read(Path.of("src/test/resources/sampling/DissModel.csv"));
        Set<Map<IConfigurable, Boolean>> configurations = doplerSampler.sampleValidConfigurations(model);
        Assertions.assertEquals(288, configurations.size());
    }
}
