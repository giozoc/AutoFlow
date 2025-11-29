package it.autoflow.configuration.service;

import it.autoflow.configuration.dto.ConfigurazioneDTO;

import java.util.List;

public interface ConfigurazioneService {

    ConfigurazioneDTO creaConfigurazione(ConfigurazioneDTO dto);

    ConfigurazioneDTO getById(Long id);

    List<ConfigurazioneDTO> getAll();

    ConfigurazioneDTO update(Long id, ConfigurazioneDTO dto);

    void delete(Long id);
}