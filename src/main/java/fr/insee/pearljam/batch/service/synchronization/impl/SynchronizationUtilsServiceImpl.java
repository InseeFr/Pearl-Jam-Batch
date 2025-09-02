package fr.insee.pearljam.batch.service.synchronization.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.HabilitationService;
import fr.insee.pearljam.batch.service.synchronization.SynchronizationUtilsService;

@Service
@RequiredArgsConstructor
public class SynchronizationUtilsServiceImpl implements SynchronizationUtilsService {
    private final ContextReferentialService contextReferentialService;
    private final HabilitationService habilitationService;

    public void checkServices() throws SynchronizationException {
        habilitationService.isAvailable();
        contextReferentialService.contextReferentialServiceIsAvailable();
    }
}
