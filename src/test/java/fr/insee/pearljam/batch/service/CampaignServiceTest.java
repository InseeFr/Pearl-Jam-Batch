package fr.insee.pearljam.batch.service;

import fr.insee.pearljam.batch.campaign.PersonType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CampaignServiceTest {

    private final CampaignService campaignService = new CampaignService();

    @Nested
    class EnsureSinglePreferredPerson {

        @Test
        void shouldKeepOnlyFirstPrivilegedPerson() {
            List<PersonType> persons = List.of(
                    uther(true),
                    arthur(true),
                    merlin(false)
            );

            List<PersonType> result = campaignService.ensureSinglePrivilegedPerson(persons);

            assertEquals(1, result.stream().filter(PersonType::isPrivileged).count());
            assertEquals(UTHER, privilegedPersonName(result)
            );
        }


        @Test
        void shouldKeepListUnchangedWhenNoPreferredPersonExists() {
            List<PersonType> persons = List.of(
                    uther(false),
                    arthur(false),
                    merlin(false)
            );

            List<PersonType> result = campaignService.ensureSinglePrivilegedPerson(persons);

            assertEquals(0, result.stream().filter(PersonType::isPrivileged).count());
            assertSame(persons, result);
        }

        @Test
        void shouldHandleEmptyList() {
            List<PersonType> persons = List.of();
            List<PersonType> result = campaignService.ensureSinglePrivilegedPerson(persons);

            assertEquals(0, result.stream().filter(PersonType::isPrivileged).count());
        }

    }

    private static final String UTHER = "Uther";
    private static final String ARTHUR = "Arthur";
    private static final String MERLIN = "Merlin";

    private static PersonType uther(boolean preferred) {
        return person(preferred, UTHER);
    }

    private static PersonType arthur(boolean preferred) {
        return person(preferred, ARTHUR);
    }

    private static PersonType merlin(boolean preferred) {
        return person(preferred, MERLIN);
    }


    private static PersonType person(boolean privileged, String firstName) {
        PersonType person = new PersonType();
        person.setFirstName(firstName);
        person.setPrivileged(privileged);
        return person;
    }

    private static String privilegedPersonName(List<PersonType> persons) {
        return persons.stream()
                .filter(PersonType::isPrivileged)
                .findFirst()
                .orElseThrow()
                .getFirstName();
    }
}