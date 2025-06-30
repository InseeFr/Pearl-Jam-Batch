package fr.insee.pearljam.batch.service.synchronization;

import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.exception.SynchronizationException;

@Service
public interface SynchronizationUtilsService {

    void checkServices() throws SynchronizationException;
}
