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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.easy.extension.QmConstants;

/**
 * Accesses all constants.
 * 
 * @author Holger Eichelberger
 */
public class QmConstantsTest {
    
    /**
     * Just tests readability of all constants.
     * 
     * @throws IllegalAccessException shall not occur
     * @throws IllegalArgumentException shall not occur 
     */
    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = QmConstants.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
                Assert.assertNotNull(f.get(null)); // static
            }
        }
    }

}
