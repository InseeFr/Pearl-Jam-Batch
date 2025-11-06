package fr.insee.pearljam.batch.service.impl;
import fr.insee.pearljam.batch.campaign.PersonType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommunicationServiceImplTest {

    private CommunicationServiceImpl communicationService;

    @BeforeEach
    void setUp() {
        communicationService = new CommunicationServiceImpl(   null, null, null, null, null,
                null, null, null, null, null);
    }

    @Test
    void testSimpleFemaleName()  {
        PersonType person = new MockPerson("MISS", "Alice", "Durand");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME Alice Durand", result);
    }

    @Test
    void testSimpleMaleName()  {
        PersonType person = new MockPerson("MR", "Pierre", "Martin");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M Pierre Martin", result);
    }

    @Test
    void testHyphenatedFemaleName()  {
        PersonType person = new MockPerson("MISS", "Marie-Claire", "Moreau");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME Marie-Claire Moreau", result);
    }

    @Test
    void testMaleComposedNameKeepFirst()  {
        PersonType person = new MockPerson("MR", "Jean Michel Louis", "DupontelEstVraimentLong");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M Jean DupontelEstVraimentLong", result);
    }

    @Test
    void testMaleComposedFirstNameFallbackToAcronym()  {
        PersonType person = new MockPerson("MR", "Maxence Michel-Arnaud", "DupontelleEstVraimentTrèsLong");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M M.M.A DupontelleEstVraimentTrèsLong", result);
    }

    @Test
    void testFemaleKeepOnlyName()  {
        PersonType person = new MockPerson("MISS", "Catherine", "DucheminVraimentTrèsLongNomComposé");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME DucheminVraimentTrèsLongNomComposé", result);
    }

    @Test
    void testFemaleNameFallbackToTruncation()  {
        PersonType person = new MockPerson("MISS", "Catherine", "DeLaVallièreVraimentTrèsLongNomComposé");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME DeLaVallièreVraimentTrèsLongNomCom", result);
        assertEquals(38, result.length());
    }

    // Helper mock implementation of PersonType
    static class MockPerson extends PersonType {
        private final String title;
        private final String firstName;
        private final String lastName;

        public MockPerson(String title, String firstName, String lastName) {
            this.title = title;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }
    }
}
