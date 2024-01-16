package dk.dbc.ocbtools.testengine.asserters;

import dk.dbc.oss.ns.catalogingupdate.MessageEntry;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UpdateAsserterTest {
    @Test
    public void testAssertValidationEqual() {
        // TODO: this test doesn't really make much sense now
        MessageEntry messageEntry1 = new MessageEntry();
        messageEntry1.setOrdinalPositionOfField(4);
        messageEntry1.setOrdinalPositionInSubfield(1);
        messageEntry1.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry1.setUrlForDocumentation("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<MessageEntry> expectedList = new ArrayList<>();
        expectedList.add(messageEntry1);
        assertTrue(expectedList.size() == 1);

        MessageEntry messageEntry2 = new MessageEntry();
        messageEntry2.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry2.setUrlForDocumentation("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        messageEntry2.setOrdinalPositionInSubfield(1);
        messageEntry2.setOrdinalPositionOfField(4);
        List<MessageEntry> actualList = new ArrayList<>();
        actualList.add(messageEntry2);
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
        List<MessageEntry> expectedList = null;
        List<MessageEntry> actualList = null;
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
        List<MessageEntry> expectedList = new ArrayList<>();
        assertTrue(expectedList.size() == 0);
        List<MessageEntry> actualList = new ArrayList<>();
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
        MessageEntry messageEntry1 = new MessageEntry();
        messageEntry1.setOrdinalPositionOfField(4);
        messageEntry1.setOrdinalPositionInSubfield(1);
        messageEntry1.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry1.setUrlForDocumentation("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<MessageEntry> expectedList = new ArrayList<>();
        expectedList.add(messageEntry1);
        assertTrue(expectedList.size() == 1);

        MessageEntry messageEntry2 = new MessageEntry();
        messageEntry2.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry2.setOrdinalPositionInSubfield(1);
        messageEntry2.setOrdinalPositionOfField(4);
        List<MessageEntry> actualList = new ArrayList<>();
        actualList.add(messageEntry2);
        assertTrue(actualList.size() == 1);
        //noinspection Duplicates
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
        MessageEntry messageEntry1 = new MessageEntry();
        messageEntry1.setOrdinalPositionOfField(4);
        messageEntry1.setOrdinalPositionInSubfield(1);
        messageEntry1.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry1.setUrlForDocumentation("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<MessageEntry> expectedList = new ArrayList<>();
        expectedList.add(messageEntry1);
        assertTrue(expectedList.size() == 1);

        MessageEntry messageEntry2 = new MessageEntry();
        messageEntry2.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry2.setOrdinalPositionInSubfield(1);
        messageEntry2.setOrdinalPositionOfField(4);
        List<MessageEntry> actualList = null;
        //noinspection Duplicates
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
        MessageEntry messageEntry1 = new MessageEntry();
        messageEntry1.setOrdinalPositionOfField(4);
        messageEntry1.setOrdinalPositionInSubfield(1);
        messageEntry1.setMessage("Posten med faustnr/bibliotek '51617649/870970' findes ikke i forvejen");
        messageEntry1.setUrlForDocumentation("http://www.kat-format.dk/danMARC2/Danmarc2.f.htm");
        List<MessageEntry> expectedList = new ArrayList<>();
        expectedList.add(messageEntry1);
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
