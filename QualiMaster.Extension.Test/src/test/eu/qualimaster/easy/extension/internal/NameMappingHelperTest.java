/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.eu.qualimaster.easy.extension.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import eu.qualimaster.coordination.CoordinationManager;
import eu.qualimaster.coordination.INameMapping;
import eu.qualimaster.coordination.NameMapping;
import eu.qualimaster.easy.extension.internal.NameMappingHelper;
import eu.qualimaster.easy.extension.internal.SubPipelineHelper;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the name mapping helpers.
 * 
 * @author Holger Eichelberger
 */
public class NameMappingHelperTest {

    /**
     * Tests the name mapping helper.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testNameMapping() throws IOException {
        final String pipName = "PriorityPip";
        INameMapping mapping = loadNameMapping("genSubTopoMapping.xml", pipName);
        CoordinationManager.registerTestMapping(mapping);
        Assert.assertEquals("PriorityPip_FamilyElement0", 
            NameMappingHelper.getImplementationName(pipName, "FinancialCorrelation"));
        Assert.assertEquals("FinancialCorrelation1", 
            NameMappingHelper.getImplementationName(pipName, "FinancialCorrelation1"));
        Assert.assertEquals("FinancialCorrelation1", 
            NameMappingHelper.getImplementationName("MyPip", "FinancialCorrelation1"));
        CoordinationManager.unregisterNameMapping(mapping);
    }

    /**
     * Tests the sub-pipeline helper on a mapping file which does not expose a sub-pipeline.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIsSubPipeline1() throws IOException {
        final String pipName = "PriorityPip";
        INameMapping mapping = loadNameMapping("genSubTopoMapping.xml", pipName);
        CoordinationManager.registerTestMapping(mapping);
        Assert.assertEquals(false, SubPipelineHelper.isSubPipeline(pipName, "aaa"));
        CoordinationManager.unregisterNameMapping(mapping);
    }

    /**
     * Tests the sub-pipeline helper on a mapping file which does not expose a sub-pipeline.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testIsSubPipeline2() throws IOException {
        final String pipName = "RandomPip";
        INameMapping mapping = loadNameMapping("randomSubTopoMapping.xml", pipName);
        CoordinationManager.registerTestMapping(mapping);
        Assert.assertEquals(false, SubPipelineHelper.isSubPipeline(pipName, "RandomPip"));
        Assert.assertEquals(true, SubPipelineHelper.isSubPipeline(pipName, "RandomSubPipeline1"));
        CoordinationManager.unregisterNameMapping(mapping);
    }

    /**
     * Loads a given name mapping.
     * 
     * @param fileName the file name within the testdata folder
     * @param pipName the pipeline name
     * @return the name mapping
     * @throws IOException in case of I/O problems
     */
    static INameMapping loadNameMapping(String fileName, String pipName) throws IOException {
        NameMapping mapping;
        File testdir = new File(System.getProperty("qm.base.dir", "."), "testdata");
        File file = new File(testdir, fileName);
        FileInputStream in = new FileInputStream(file);
        mapping = new NameMapping(pipName, in);
        in.close();
        return mapping;
    }

}
