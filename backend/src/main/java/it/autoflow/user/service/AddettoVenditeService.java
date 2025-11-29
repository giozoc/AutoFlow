package it.autoflow.user.service;

import it.autoflow.commons.service.CrudService;
import it.autoflow.user.entity.AddettoVendite;

public interface AddettoVenditeService extends CrudService<AddettoVendite, Long> {

    boolean resetPassword(Long addettoId);

    AddettoVendite deactivateAddetto(Long addettoId);
}