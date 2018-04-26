package edu.sdsu.cs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Tom Paulus
 * Created on 4/13/18.
 */
public class LineProcessorTest {
    @Test
    public void getEng() {
        assertNotNull(LineProcessor.getInstance().getEng());
    }

    @Test
    public void testSimpleMATLABFunction() throws Exception {
        double[] a = {2.0, 4.0, 6.0};
        double[] expected = {1.4142135623730951, 2.0, 2.449489742783178};
        double[] roots = LineProcessor.getInstance().getEng().feval("sqrt", a);
        assertEquals(expected[0], roots[0], 0.001d);
        assertEquals(expected[1], roots[1], 0.001d);
        assertEquals(expected[2], roots[2], 0.001d);

        for (double e : roots) {
            System.out.println(e);
        }
    }

    @Test
    public void testExternalMATLABFunction() throws Exception{
        double a = 1;
        double expected = a;

        double result = LineProcessor.getInstance().getEng().feval("test_function", a);
        assertEquals(expected, a, 0.001d);
    }
}