/* Copyright (C) 2013-2020 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.util.ts.modal.regression;

import java.io.IOException;
import java.io.StringWriter;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.MTSTransition;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import net.automatalib.ts.modal.transition.MutableModalEdgeProperty;
import net.automatalib.util.ts.modal.MCUtil;
import net.automatalib.util.ts.modal.MTSUtil;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RegressionTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegressionTests.class);

    @DataProvider(name = "Decomp")
    public Object[][] loadDecompositionTests() {
        LOGGER.info("Found {} decomposition tests", RegressionTestBundles.DECOMPOSITION_TESTS.size());
        Object[][] params = new Object[RegressionTestBundles.DECOMPOSITION_TESTS.size()][1];
        int i = 0;
        for (DecompositionTest testCase : RegressionTestBundles.DECOMPOSITION_TESTS) {
            params[i++] = new Object[] {testCase};
        }
        return params;
    }

    @DataProvider(name = "Comp")
    public Object[][] loadCompositionTests() {
        LOGGER.info("Found {} composition tests", RegressionTestBundles.COMPOSITION_TESTS.size());
        Object[][] params = new Object[RegressionTestBundles.COMPOSITION_TESTS.size()][1];
        int i = 0;
        for (CompositionTest testCase : RegressionTestBundles.COMPOSITION_TESTS) {
            params[i++] = new Object[] {testCase};
        }
        return params;
    }

    @Test(dataProvider = "Comp")
    public void testMerge(CompositionTest testCase) throws IOException {
        final CompositionInstance instance = new CompositionInstance(testCase);

        LOGGER.debug("Testing merge for " + testCase.merge);
        LOGGER.debug("Components: {}, {}", testCase.input0, testCase.input1);
        LOGGER.debug("Input alphabets: [{}] [{}]",
                     instance.input0.getInputAlphabet(),
                     instance.input1.getInputAlphabet());

        CompactMTS<String> currentMerge = MTSUtil.compose(instance.input0, instance.input1);

        Assertions.assertThat(currentMerge.getInputAlphabet())
                  .containsExactlyInAnyOrderElementsOf(instance.merge.getInputAlphabet());
        Assertions.assertThat(instance.merge.getInputAlphabet())
                  .containsExactlyInAnyOrderElementsOf(currentMerge.getInputAlphabet());
        Assert.assertTrue(MTSUtil.isRefinementOf(currentMerge, instance.merge, currentMerge.getInputAlphabet()));
        Assert.assertTrue(MTSUtil.isRefinementOf(instance.merge, currentMerge, currentMerge.getInputAlphabet()));
    }

    @Test(dataProvider = "Decomp")
    public void testDecompContext(DecompositionTest testCase) throws IOException {
        if (testCase.modalContract == null) {
            return;
        }
        final DecompositionInstance instance = new DecompositionInstance(testCase);

        LOGGER.debug("Testing decomp for " + testCase.origSys);
        LOGGER.debug("Modal contract " + testCase.modalContract);
        LOGGER.debug("Components: {}, {}", testCase.context, testCase.system);
        LOGGER.debug("Com. alphabet: {}", instance.modalContract.getCommunicationAlphabet());

        MCUtil.SystemComponent<CompactMTS<String>, Integer, String, MTSTransition<String, MutableModalEdgeProperty>, MutableModalEdgeProperty>
                systemComponent = MCUtil.systemComponent(instance.modalContract,
                                                         new CompactMTS.Creator<>(),
                                                         (x) -> new ModalEdgePropertyImpl(x.getProperty().getType()),
                                                         () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> redLanguage = (CompactDFA<String>) MCUtil.redContextLanguage(systemComponent,
                                                                                        instance.modalContract.getCommunicationAlphabet());
        CompactMTS<String> redContext = MCUtil.redContextComponent(redLanguage,
                                                                   new CompactMTS.Creator<>(),
                                                                   instance.modalContract.getCommunicationAlphabet(),
                                                                   () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> greenLanguage = (CompactDFA<String>) MCUtil.greenContextLanguage(instance.modalContract);
        CompactMTS<String> greenContext = MCUtil.greenContextComponent(greenLanguage,
                                                                       new CompactMTS.Creator<>(),
                                                                       instance.modalContract.getCommunicationAlphabet(),
                                                                       () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY),
                                                                       () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST));

        CompactMTS<String> context = MTSUtil.conjunction(greenContext, redContext);

        Assert.assertTrue(MTSUtil.isRefinementOf(context, instance.context, context.getInputAlphabet()));
    }

    @Test(dataProvider = "Decomp")
    public void testDecompContextReverse(DecompositionTest testCase) throws IOException {
        if (testCase.modalContract == null) {
            return;
        }
        final DecompositionInstance instance = new DecompositionInstance(testCase);

        LOGGER.debug("Testing decomp for " + testCase.origSys);
        LOGGER.debug("Modal contract " + testCase.modalContract);
        LOGGER.debug("Components: {}, {}", testCase.context, testCase.system);
        LOGGER.debug("Com. alphabet: {}", instance.modalContract.getCommunicationAlphabet());

        MCUtil.SystemComponent<CompactMTS<String>, Integer, String, MTSTransition<String, MutableModalEdgeProperty>, MutableModalEdgeProperty>
                systemComponent = MCUtil.systemComponent(instance.modalContract,
                                                         new CompactMTS.Creator<>(),
                                                         (x) -> new ModalEdgePropertyImpl(x.getProperty().getType()),
                                                         () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> redLanguage = (CompactDFA<String>) MCUtil.redContextLanguage(systemComponent,
                                                                                        instance.modalContract.getCommunicationAlphabet());
        CompactMTS<String> redContext = MCUtil.redContextComponent(redLanguage,
                                                                   new CompactMTS.Creator<>(),
                                                                   instance.modalContract.getCommunicationAlphabet(),
                                                                   () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        CompactDFA<String> greenLanguage = (CompactDFA<String>) MCUtil.greenContextLanguage(instance.modalContract);
        CompactMTS<String> greenContext = MCUtil.greenContextComponent(greenLanguage,
                                                                       new CompactMTS.Creator<>(),
                                                                       instance.modalContract.getCommunicationAlphabet(),
                                                                       () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY),
                                                                       () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST));

        CompactMTS<String> context = MTSUtil.conjunction(greenContext, redContext);

        if (!MTSUtil.isRefinementOf(instance.context, context, context.getInputAlphabet())) {
            final StringWriter sw = new StringWriter();
            GraphDOT.write(context.graphView(), sw);
            Assert.fail("Conjunction is not a refinement of model: " + sw.toString());
        }
    }

    @Test(dataProvider = "Decomp")
    public void testDecompSystem(DecompositionTest testCase) throws IOException {
        if (testCase.modalContract == null) {
            return;
        }
        final DecompositionInstance instance = new DecompositionInstance(testCase);

        LOGGER.debug("Testing decomp for " + testCase.origSys);
        LOGGER.debug("Modal contract " + testCase.modalContract);
        LOGGER.debug("Components: {}, {}", testCase.context, testCase.system);
        LOGGER.debug("Com. alphabet: {}", instance.modalContract.getCommunicationAlphabet());

        MCUtil.SystemComponent<CompactMTS<String>, Integer, String, MTSTransition<String, MutableModalEdgeProperty>, MutableModalEdgeProperty>
                systemComponent = MCUtil.systemComponent(instance.modalContract,
                                                         new CompactMTS.Creator<>(),
                                                         (x) -> new ModalEdgePropertyImpl(x.getProperty().getType()),
                                                         () -> new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY));

        Assert.assertTrue(MTSUtil.isRefinementOf(instance.system,
                                                 systemComponent.systemComponent,
                                                 systemComponent.systemComponent.getInputAlphabet()));
    }

}
