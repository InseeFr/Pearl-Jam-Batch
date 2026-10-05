package fr.insee.pearljam.batch.service.impl;

import fr.insee.pearljam.batch.campaign.PersonType;
import fr.insee.pearljam.batch.campaign.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommunicationServiceImplTest {

    private CommunicationServiceImpl communicationService;

    @BeforeEach
    void setUp() {
        communicationService = new CommunicationServiceImpl(   null, null, null, null, null,
                null, null, null, null, null);
    }

    @Test
    void testSimpleFemaleName()  {
        PersonType person = new MockPerson(Title.MISS, "Alice", "Durand");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME Alice Durand", result);
    }

    @Test
    void testSimpleMaleName()  {
        PersonType person = new MockPerson(Title.MISTER, "Pierre", "Martin");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M Pierre Martin", result);
    }

    @Test
    void testHyphenatedFemaleName()  {
        PersonType person = new MockPerson(Title.MISS, "Marie-Claire", "Moreau");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME Marie-Claire Moreau", result);
    }

    @Test
    void testMaleComposedNameKeepFirst()  {
        PersonType person = new MockPerson(Title.MISTER, "Jean Michel Louis", "DupontelEstVraimentLong");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M Jean DupontelEstVraimentLong", result);
    }

    @Test
    void testMaleComposedFirstNameFallbackToAcronym()  {
        PersonType person = new MockPerson(Title.MISTER, "Maxence Michel-Arnaud", "DupontelleEstVraimentTrèsLong");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M M.M.A DupontelleEstVraimentTrèsLong", result);
    }

    @Test
    void testFemaleKeepOnlyName()  {
        PersonType person = new MockPerson(Title.MISS, "Catherine", "DucheminVraimentTrèsLongNomComposé");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME DucheminVraimentTrèsLongNomComposé", result);
    }

    @Test
    void testFemaleNameFallbackToTruncation()  {
        PersonType person = new MockPerson(Title.MISS, "Catherine", "DeLaVallièreVraimentTrèsLongNomComposé");
        String result = communicationService.generateRecipientName(person);
        assertEquals("MME DeLaVallièreVraimentTrèsLongNomCom", result);
        assertEquals(38, result.length());
    }

    @Test
    void testUndefinedTitle() {
        PersonType person = new MockPerson(Title.UNDEFINED, "Jean", "Dupont");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M ou Mme Jean Dupont", result);
    }

    @Test
    void testUndefinedTitleWithLongName() {
        // "M ou Mme Jean DupontVraimentLong" = 34 chars, should fit
        PersonType person = new MockPerson(Title.UNDEFINED, "Jean", "DupontVraimentLong");
        String result = communicationService.generateRecipientName(person);
        assertEquals("M ou Mme Jean DupontVraimentLong", result);
    }

    // Helper mock implementation of PersonType
    static class MockPerson extends PersonType {
        private final Title title;
        private final String firstName;
        private final String lastName;

        public MockPerson(Title title, String firstName, String lastName) {
            this.title = title;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public Title getTitle() {
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
