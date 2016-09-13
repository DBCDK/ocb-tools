package dk.dbc.ocbtools.testengine.asserters;

import dk.dbc.updateservice.service.api.Entry;
import dk.dbc.updateservice.service.api.Param;
import dk.dbc.updateservice.service.api.Params;
import dk.dbc.updateservice.service.api.Type;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UpdateAsserterTest {
    @Test
    public void testAssertValidationEqual() {
        Param paramE1 = new Param();
        paramE1.setKey("fieldno");
        paramE1.setValue("4");
        Param paramE2 = new Param();
        paramE2.setKey("subfieldno");
        paramE2.setValue("1");
        Param paramE3 = new Param();
        paramE3.setKey("message");
        paramE3.setValue("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        Param paramE4 = new Param();
        paramE4.setKey("url");
        paramE4.setValue("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<Entry> expectedList = new ArrayList<>();
        expectedList.add(new Entry());
        expectedList.get(0).setType(Type.ERROR);
        expectedList.get(0).setParams(new Params());
        expectedList.get(0).getParams().getParam().add(paramE1);
        expectedList.get(0).getParams().getParam().add(paramE2);
        expectedList.get(0).getParams().getParam().add(paramE3);
        expectedList.get(0).getParams().getParam().add(paramE4);
        assertTrue(expectedList.size() == 1);

        Param paramA1 = new Param();
        paramA1.setKey("message");
        paramA1.setValue("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        Param paramA2 = new Param();
        paramA2.setKey("url");
        paramA2.setValue("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        Param paramA3 = new Param();
        paramA3.setKey("subfieldno");
        paramA3.setValue("1");
        Param paramA4 = new Param();
        paramA4.setKey("fieldno");
        paramA4.setValue("4");
        List<Entry> actualList = new ArrayList<>();
        actualList.add(new Entry());
        actualList.get(0).setType(Type.ERROR);
        actualList.get(0).setParams(new Params());
        actualList.get(0).getParams().getParam().add(paramA1);
        actualList.get(0).getParams().getParam().add(paramA2);
        actualList.get(0).getParams().getParam().add(paramA3);
        actualList.get(0).getParams().getParam().add(paramA4);
        assertTrue(actualList.size() == 1);

        try {
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, expectedList, actualList);
        } catch (AssertionError e) {
            fail("AssertionError, this should not happen");
        } catch (IOException e) {
            fail("IOException, this should not happen");
        }
    }

    @Test
    public void testAssertValidationEqualNull() {
        List<Entry> expectedList = null;
        List<Entry> actualList = null;
        try {
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, expectedList, actualList);
            assertTrue(expectedList == actualList);
        } catch (AssertionError e) {
            fail("AssertionError, this should not happen");
        } catch (IOException e) {
            fail("IOException, this should not happen");
        }
    }

    @Test
    public void testAssertValidationEqualEmpty() {
        List<Entry> expectedList = new ArrayList<>();
        assertTrue(expectedList.size() == 0);
        List<Entry> actualList = new ArrayList<>();
        assertTrue(actualList.size() == 0);
        try {
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, expectedList, actualList);
            assertTrue(expectedList.size() == actualList.size());
        } catch (AssertionError e) {
            fail("AssertionError, this should not happen");
        } catch (IOException e) {
            fail("IOException, this should not happen");
        }
    }

    @Test
    public void testAssertValidationNotEqual() {
        Param paramE1 = new Param();
        paramE1.setKey("fieldno");
        paramE1.setValue("4");
        Param paramE2 = new Param();
        paramE2.setKey("subfieldno");
        paramE2.setValue("1");
        Param paramE3 = new Param();
        paramE3.setKey("message");
        paramE3.setValue("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        Param paramE4 = new Param();
        paramE4.setKey("url");
        paramE4.setValue("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<Entry> expectedList = new ArrayList<>();
        expectedList.add(new Entry());
        expectedList.get(0).setType(Type.ERROR);
        expectedList.get(0).setParams(new Params());
        expectedList.get(0).getParams().getParam().add(paramE1);
        expectedList.get(0).getParams().getParam().add(paramE2);
        expectedList.get(0).getParams().getParam().add(paramE3);
        expectedList.get(0).getParams().getParam().add(paramE4);
        assertTrue(expectedList.size() == 1);

        Param paramA1 = new Param();
        paramA1.setKey("message");
        paramA1.setValue("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        Param paramA3 = new Param();
        paramA3.setKey("subfieldno");
        paramA3.setValue("1");
        Param paramA4 = new Param();
        paramA4.setKey("fieldno");
        paramA4.setValue("4");
        List<Entry> actualList = new ArrayList<>();
        actualList.add(new Entry());
        actualList.get(0).setType(Type.ERROR);
        actualList.get(0).setParams(new Params());
        actualList.get(0).getParams().getParam().add(paramA1);
        actualList.get(0).getParams().getParam().add(paramA3);
        actualList.get(0).getParams().getParam().add(paramA4);
        assertTrue(actualList.size() == 1);

        try {
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, expectedList, actualList);
            fail("We should have thrown an error by now");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().startsWith("Validation error"));
        } catch (IOException e) {
            fail("IOException, this should not happen");
        }
    }

    @Test
    public void testAssertValidationNotEqualOneNull() {
        Param paramE1 = new Param();
        paramE1.setKey("fieldno");
        paramE1.setValue("4");
        Param paramE2 = new Param();
        paramE2.setKey("subfieldno");
        paramE2.setValue("1");
        Param paramE3 = new Param();
        paramE3.setKey("message");
        paramE3.setValue("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        Param paramE4 = new Param();
        paramE4.setKey("url");
        paramE4.setValue("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<Entry> expectedList = new ArrayList<>();
        expectedList.add(new Entry());
        expectedList.get(0).setType(Type.ERROR);
        expectedList.get(0).setParams(new Params());
        expectedList.get(0).getParams().getParam().add(paramE1);
        expectedList.get(0).getParams().getParam().add(paramE2);
        expectedList.get(0).getParams().getParam().add(paramE3);
        expectedList.get(0).getParams().getParam().add(paramE4);
        assertTrue(expectedList.size() == 1);
        List<Entry> actualList = null;

        try {
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, expectedList, actualList);
            fail("We should have thrown an error by now");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().startsWith("Validation error"));
        } catch (IOException e) {
            fail("IOException, this should not happen");
        }
    }

    @Test
    public void testAssertValidationNotEqualOneEmpty() {
        Param paramE1 = new Param();
        paramE1.setKey("fieldno");
        paramE1.setValue("4");
        Param paramE2 = new Param();
        paramE2.setKey("subfieldno");
        paramE2.setValue("1");
        Param paramE3 = new Param();
        paramE3.setKey("message");
        paramE3.setValue("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        Param paramE4 = new Param();
        paramE4.setKey("url");
        paramE4.setValue("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<Entry> expectedList = new ArrayList<>();
        expectedList.add(new Entry());
        expectedList.get(0).setType(Type.ERROR);
        expectedList.get(0).setParams(new Params());
        expectedList.get(0).getParams().getParam().add(paramE1);
        expectedList.get(0).getParams().getParam().add(paramE2);
        expectedList.get(0).getParams().getParam().add(paramE3);
        expectedList.get(0).getParams().getParam().add(paramE4);
        assertTrue(expectedList.size() == 1);
        try {
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, expectedList, new ArrayList<>());
            fail("We should have thrown an error by now");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().startsWith("Validation error"));
        } catch (IOException e) {
            fail("IOException, this should not happen");
        }
    }
}
