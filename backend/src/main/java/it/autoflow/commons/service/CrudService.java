package it.autoflow.commons.service;

import java.util.List;

/**
 * Servizio generico per operazioni CRUD su un'entità di dominio.
 * Corrisponde all'interfaccia CrudService<T, ID> descritta nell'ODD.
 */
public interface CrudService<T, ID> {

    /**
     * Crea una nuova entità.
     *
     * @param entity entità da persistere
     * @return entità salvata
     */
    T create(T entity);

    /**
     * Recupera un'entità tramite ID.
     *
     * @param id identificativo dell'entità
     * @return entità trovata oppure null se non esiste
     */
    T getById(ID id);

    /**
     * Aggiorna un'entità esistente.
     *
     * @param id     identificativo dell'entità da aggiornare
     * @param entity dati aggiornati
     * @return entità aggiornata
     */
    T update(ID id, T entity);

    /**
     * Elimina un'entità tramite ID.
     *
     * @param id identificativo dell'entità
     * @return true se l'entità è stata eliminata, false altrimenti
     */
    boolean delete(ID id);

    /**
     * Restituisce tutte le entità del tipo gestito.
     *
     * @return lista di tutte le entità
     */
    List<T> findAll();
}