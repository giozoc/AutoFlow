package it.autoflow.statistics.service;

import it.autoflow.invoice.repository.FatturaRepository;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.statistics.dto.DashboardStatisticsDTO;
import it.autoflow.statistics.service.StatisticsService;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.repository.VeicoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ClienteRepository clienteRepository;
    private final VeicoloRepository veicoloRepository;
    private final PropostaRepository propostaRepository;
    private final FatturaRepository fatturaRepository;

    @Override
    public DashboardStatisticsDTO getDashboardStatistics() {
        DashboardStatisticsDTO dto = new DashboardStatisticsDTO();

        // --- conteggi base ---
        dto.setTotaleClienti(clienteRepository.count());
        dto.setTotaleVeicoli(veicoloRepository.count());
        dto.setTotaleProposte(propostaRepository.count());
        dto.setTotaleFatture(fatturaRepository.count());

        // --- proposte per stato ---
        dto.setProposteBozza(propostaRepository.countByStato(StatoProposta.BOZZA));
        dto.setProposteInviate(propostaRepository.countByStato(StatoProposta.INVIATA));
        dto.setProposteAccettate(propostaRepository.countByStato(StatoProposta.ACCETTATA));
        dto.setProposteRifiutate(propostaRepository.countByStato(StatoProposta.RIFIUTATA));
        dto.setProposteScadute(propostaRepository.countByStato(StatoProposta.SCADUTA));
        dto.setProposteAnnullate(propostaRepository.countByStato(StatoProposta.ANNULLATA));
        dto.setProposteCompletate(propostaRepository.countByStato(StatoProposta.COMPLETATA));

        // --- fatturato ---
        LocalDate oggi = LocalDate.now();
        int anno = oggi.getYear();
        int mese = oggi.getMonthValue();

        dto.setFatturatoTotale(fatturaRepository.sumImportoTotale());
        dto.setFatturatoAnnoCorrente(fatturaRepository.sumImportoTotaleByAnno(anno));
        dto.setFatturatoMeseCorrente(
                fatturaRepository.sumImportoTotaleByAnnoAndMese(anno, mese)
        );

        // --- fatture non pagate ---
        dto.setFattureNonPagate(fatturaRepository.countByDataPagamentoIsNull());

        return dto;
    }
}