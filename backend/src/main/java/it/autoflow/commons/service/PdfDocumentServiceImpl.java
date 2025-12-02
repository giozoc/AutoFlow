package it.autoflow.commons.service;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.commons.repository.DocumentoPDFRepository;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.entity.OptionalAccessorio;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.invoice.repository.FatturaRepository;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.user.entity.Cliente;
import it.autoflow.vehicle.entity.Veicolo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.awt.Color;

@Service
@RequiredArgsConstructor
public class PdfDocumentServiceImpl implements PdfDocumentService {

    private static final PDType1Font FONT_TITLE =
            new PDType1Font(FontName.HELVETICA_BOLD);

    private static final PDType1Font FONT_TEXT =
            new PDType1Font(FontName.HELVETICA);

    private final PropostaRepository propostaRepository;
    private final FatturaRepository fatturaRepository;
    private final DocumentoPDFRepository documentoPDFRepository;
    private final FileStorageService fileStorageService;

    // -------------------------------------------------------------
    //  PROPOSTA → PDF
    // -------------------------------------------------------------
    @Override
    public DocumentoPDF generateProposalPdf(Long propostaId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proposta non trovata con id: " + propostaId));

        byte[] pdfBytes = buildProposalPdf(proposta);

        String fileName = "proposta-" + proposta.getId() + ".pdf";
        LocalDate data = proposta.getDataCreazione() != null
                ? proposta.getDataCreazione().toLocalDate()
                : LocalDate.now();
        String folderKey = String.format("proposte/%d/%02d", data.getYear(), data.getMonthValue());

        // usa FileStorageService per salvare
        String path = saveBytesWithStorage(pdfBytes, fileName, folderKey);

        DocumentoPDF documento = new DocumentoPDF();
        documento.setNomeFile(fileName);
        documento.setPath(path);
        documento.setDimensioneBytes((long) pdfBytes.length);
        documento.setDataCreazione(LocalDateTime.now());
        documento.setUltimaModifica(LocalDateTime.now());

        documento = documentoPDFRepository.save(documento);

        // Nota: attualmente Proposta NON ha un campo DocumentoPDF.
        // Se vuoi collegarlo, aggiungi in Proposta: @OneToOne private DocumentoPDF documentoPdf;

        return documento;
    }

    // -------------------------------------------------------------
    //  FATTURA → PDF
    // -------------------------------------------------------------
    @Override
    public DocumentoPDF generateInvoicePdf(Long fatturaId) {
        Fattura fattura = fatturaRepository.findById(fatturaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fattura non trovata con id: " + fatturaId));

        byte[] pdfBytes = buildInvoicePdf(fattura);

        LocalDate data = fattura.getDataEmissione() != null
                ? fattura.getDataEmissione()
                : LocalDate.now();

        String fileName = "fattura-" + fattura.getNumeroFattura() + ".pdf";
        String folderKey = String.format("fatture/%d/%02d", data.getYear(), data.getMonthValue());

        String path = saveBytesWithStorage(pdfBytes, fileName, folderKey);

        DocumentoPDF documento = fattura.getDocumentoPdf();
        LocalDateTime now = LocalDateTime.now();

        if (documento == null) {
            documento = new DocumentoPDF();
            documento.setDataCreazione(now);
        }
        documento.setNomeFile(fileName);
        documento.setPath(path);
        documento.setDimensioneBytes((long) pdfBytes.length);
        documento.setUltimaModifica(now);

        documento = documentoPDFRepository.save(documento);

        // collega il PDF alla fattura
        fattura.setDocumentoPdf(documento);
        fatturaRepository.save(fattura);

        return documento;
    }

    // -------------------------------------------------------------
    //  RE-GENERATE (implementazione minimale)
    // -------------------------------------------------------------
    @Override
    public DocumentoPDF regeneratePdf(Long documentoId) {
        DocumentoPDF documento = documentoPDFRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DocumentoPDF non trovato con id: " + documentoId));

        // Implementazione "light": aggiorno solo il timestamp.
        // Se vuoi rigenerare davvero il file, devi capire se è legato
        // a una Fattura o a una Proposta e richiamare il metodo giusto.
        documento.setUltimaModifica(LocalDateTime.now());
        return documentoPDFRepository.save(documento);
    }

    // -------------------------------------------------------------
    //  Helpers
    // -------------------------------------------------------------
    private String saveBytesWithStorage(byte[] bytes, String fileName, String folderKey) {
        // uso il metodo extra di FileStorageServiceImpl
        if (fileStorageService instanceof FileStorageServiceImpl impl) {
            return impl.store(bytes, fileName, folderKey);
        }

        // fallback: temp file + store(File,...)
        try {
            File temp = File.createTempFile("autoflow-", "-" + fileName);
            java.nio.file.Files.write(temp.toPath(), bytes);
            return fileStorageService.store(temp, folderKey);
        } catch (IOException e) {
            throw new RuntimeException("Errore nel salvataggio PDF", e);
        }
    }

    private byte[] buildProposalPdf(Proposta proposta) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 750;

                cs.beginText();
                cs.setFont(FONT_TITLE, 18);
                cs.newLineAtOffset(50, y);
                cs.showText("Proposta n. " + proposta.getId());
                cs.endText();

                y -= 40;

                cs.beginText();
                cs.setFont(FONT_TEXT, 12);
                cs.newLineAtOffset(50, y);
                cs.showText("Cliente: " +
                        (proposta.getCliente() != null
                                ? proposta.getCliente().getNome() + " " + proposta.getCliente().getCognome()
                                : "N/D"));
                cs.endText();

                y -= 20;

                cs.beginText();
                cs.setFont(FONT_TEXT, 12);
                cs.newLineAtOffset(50, y);
                cs.showText("Prezzo proposta: " + proposta.getPrezzoProposta() + " EUR");
                cs.endText();

                if (proposta.getConfigurazione() != null) {
                    y -= 20;
                    cs.beginText();
                    cs.setFont(FONT_TEXT, 12);
                    cs.newLineAtOffset(50, y);
                    cs.showText("Prezzo configurazione: " +
                            proposta.getConfigurazione().getPrezzoTotale() + " EUR");
                    cs.endText();
                }

                // puoi aggiungere altre righe (note, stato, ecc.)
            }

            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Errore nella generazione del PDF della proposta", e);
        }
    }

    private byte[] buildInvoicePdf(Fattura fattura) {
        // Font (PDFBox 3.x)
        PDType1Font fontTitle = new PDType1Font(FontName.HELVETICA_BOLD);
        PDType1Font fontText  = new PDType1Font(FontName.HELVETICA);

        java.text.NumberFormat currency =
                java.text.NumberFormat.getCurrencyInstance(java.util.Locale.ITALY);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageWidth  = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin     = 40f;

            Cliente cliente = fattura.getCliente();
            Proposta proposta = fattura.getProposta();
            Configurazione configurazione = proposta != null ? proposta.getConfigurazione() : null;
            Veicolo veicolo = configurazione != null ? configurazione.getVeicolo() : null;
            Set<OptionalAccessorio> optionals =
                    configurazione != null && configurazione.getOptional() != null
                            ? configurazione.getOptional()
                            : java.util.Collections.emptySet();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = pageHeight - margin;

                // ---------------------------------------------------------
                // TITOLO: "FATTURA <numero>" + "AutoFlow"
                // ---------------------------------------------------------
                String numero = fattura.getNumeroFattura() != null ? fattura.getNumeroFattura() : "-";
                String titolo = "FATTURA " + numero;

                cs.beginText();
                cs.setFont(fontTitle, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText(titolo);
                cs.endText();

                y -= 20;

                cs.beginText();
                cs.setFont(fontText, 12);
                cs.newLineAtOffset(margin, y);
                cs.showText("AutoFlow");
                cs.endText();

                // ---------------------------------------------------------
                // DATI CLIENTE A DESTRA
                // ---------------------------------------------------------
                float clientBoxX = pageWidth / 2 + 10;
                float clientY = pageHeight - margin;

                String nome      = cliente != null ? String.valueOf(cliente.getNome()) : "null";
                String cognome   = cliente != null ? String.valueOf(cliente.getCognome()) : "null";
                String email     = cliente != null ? String.valueOf(cliente.getEmail()) : "null";
                String telefono  = cliente != null ? String.valueOf(cliente.getTelefono()) : "null";
                String indirizzo = cliente != null ? String.valueOf(cliente.getIndirizzo()) : "null";
                String cf        = cliente != null ? String.valueOf(cliente.getCodiceFiscale()) : "null";
                String nascita   = (cliente != null && cliente.getDataNascita() != null)
                        ? cliente.getDataNascita().toString()
                        : "null";

                cs.beginText();
                cs.setFont(fontTitle, 10);
                cs.newLineAtOffset(clientBoxX, clientY);
                cs.showText("CLIENTE");
                cs.endText();

                clientY -= 14;
                cs.beginText();
                cs.setFont(fontText, 9);
                cs.newLineAtOffset(clientBoxX, clientY);
                cs.showText("Nome: " + nome);
                cs.newLineAtOffset(0, -12);
                cs.showText("Cognome: " + cognome);
                cs.newLineAtOffset(0, -12);
                cs.showText("Email: " + email);
                cs.newLineAtOffset(0, -12);
                cs.showText("Telefono: " + telefono);
                cs.newLineAtOffset(0, -12);
                cs.showText("Indirizzo: " + indirizzo);
                cs.newLineAtOffset(0, -12);
                cs.showText("Codice fiscale: " + cf);
                cs.newLineAtOffset(0, -12);
                cs.showText("Data di nascita: " + nascita);
                cs.endText();

                // ---------------------------------------------------------
                // TABELLA CENTRALE: DESCRIZIONE | PREZZO
                // ---------------------------------------------------------
                y -= 90; // un po' di spazio dopo il titolo
                float tableStartY = y;

                float colDescrWidth  = (pageWidth - 2 * margin) * 0.65f;
                float colPrezzoWidth = (pageWidth - 2 * margin) - colDescrWidth;

                float headerHeight = 20f;
                float rowHeight    = 18f;

                // header background
                cs.setNonStrokingColor(new Color(230, 230, 230));
                cs.addRect(margin, tableStartY - headerHeight,
                        colDescrWidth + colPrezzoWidth, headerHeight);
                cs.fill();
                cs.setNonStrokingColor(new Color(0, 0, 0));

                // header text
                cs.beginText();
                cs.setFont(fontText, 10);
                cs.newLineAtOffset(margin + 4, tableStartY - 14);
                cs.showText("Descrizione");
                cs.endText();

                cs.beginText();
                cs.setFont(fontText, 10);
                cs.newLineAtOffset(margin + colDescrWidth + 4, tableStartY - 14);
                cs.showText("Prezzo");
                cs.endText();

                float currentY = tableStartY - headerHeight - rowHeight;

                // RIGA VEICOLO (se esiste configurazione/veicolo)
                java.util.List<String> descs = new java.util.ArrayList<>();
                java.util.List<Double> prezzi = new java.util.ArrayList<>();

                if (configurazione != null) {
                    if (veicolo != null) {
                        String marca   = veicolo.getMarca() != null ? veicolo.getMarca() : "-";
                        String modello = veicolo.getModello() != null ? veicolo.getModello() : "-";
                        String anno    = veicolo.getAnno() != null ? veicolo.getAnno().toString() : "-";
                        String targa   = veicolo.getTarga() != null ? veicolo.getTarga() : "-";
                        String vin     = veicolo.getVin() != null ? veicolo.getVin() : "-";
                        String alim    = veicolo.getAlimentazione() != null ? veicolo.getAlimentazione().toString() : "-";
                        String cambio  = veicolo.getCambio() != null ? veicolo.getCambio().toString() : "-";
                        String colore  = veicolo.getColoreEsterno() != null ? veicolo.getColoreEsterno() : "-";
                        String km      = veicolo.getChilometraggio() != null ? veicolo.getChilometraggio().toString() : "-";

                        // riga 1: titolo veicolo + marca/modello (con prezzo)
                        descs.add(String.format("Veicolo: %s %s", marca, modello));
                        prezzi.add(configurazione.getPrezzoBase() != null ? configurazione.getPrezzoBase() : 0d);

                        // riga 2: anno/targa/VIN (senza prezzo)
                        descs.add(String.format("Anno: %s, targa: %s, VIN: %s", anno, targa, vin));
                        prezzi.add(0d);

                        // riga 3: alimentazione/cambio/colore/km (senza prezzo)
                        descs.add(String.format("Alimentazione: %s, cambio: %s, colore: %s, km: %s",
                                alim, cambio, colore, km));
                        prezzi.add(0d);
                    } else {
                        // fallback
                        descs.add("Veicolo configurato");
                        prezzi.add(configurazione.getPrezzoBase() != null ? configurazione.getPrezzoBase() : 0d);
                    }

                    // OPTIONAL
                    if (optionals != null && !optionals.isEmpty()) {
                        for (OptionalAccessorio opt : optionals) {
                            String nomeOpt = opt.getNome() != null ? opt.getNome() : "-";
                            String descOpt = opt.getDescrizione() != null ? opt.getDescrizione() : "";
                            String descFull = descOpt.isBlank()
                                    ? "Optional: " + nomeOpt
                                    : "Optional: " + nomeOpt + " - " + descOpt;

                            Double p = opt.getPrezzo() != null ? opt.getPrezzo() : 0d;
                            descs.add(descFull);
                            prezzi.add(p);
                        }
                    }
                }

                // se per qualche motivo non abbiamo niente, mettiamo una riga generica
                if (descs.isEmpty()) {
                    descs.add("Prodotto/servizio");
                    prezzi.add(fattura.getImportoTotale() != null ? fattura.getImportoTotale() : 0d);
                }

                // disegno le righe della tabella
                double totaleCalcolato = 0d;

                for (int i = 0; i < descs.size(); i++) {
                    String descr = descs.get(i);
                    Double prezzo = prezzi.get(i) != null ? prezzi.get(i) : 0d;

                    // sfondo riga
                    cs.setNonStrokingColor(new Color(245, 245, 245));
                    cs.addRect(margin, currentY + 2,
                            colDescrWidth + colPrezzoWidth, rowHeight);
                    cs.fill();
                    cs.setNonStrokingColor(Color.BLACK);

                    // descrizione
                    cs.beginText();
                    cs.setFont(fontText, 9);
                    cs.newLineAtOffset(margin + 4, currentY + 7);
                    cs.showText(descr);
                    cs.endText();

                    // prezzo (solo se > 0)
                    if (prezzo != null && prezzo > 0.0) {
                        totaleCalcolato += prezzo;

                        cs.beginText();
                        cs.setFont(fontText, 9);
                        cs.newLineAtOffset(margin + colDescrWidth + 4, currentY + 7);
                        cs.showText(formatEuro(currency, prezzo));
                        cs.endText();
                    }

                    currentY -= rowHeight;
                }

                // bordo tabella
                cs.setLineWidth(0.5f);
                cs.addRect(margin, currentY,
                        colDescrWidth + colPrezzoWidth,
                        (tableStartY - currentY));
                cs.stroke();

                // ---------------------------------------------------------
                // IMPORTO TOTALE (alla fine della tabella)
                // ---------------------------------------------------------
                double totale = fattura.getImportoTotale() != null
                        ? fattura.getImportoTotale()
                        : totaleCalcolato;

                currentY -= 30;

                cs.beginText();
                cs.setFont(fontTitle, 12);
                cs.newLineAtOffset(margin + colDescrWidth, currentY);
                cs.showText("Importo totale: " + formatEuro(currency, totale));
                cs.endText();
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Errore nella generazione del PDF della fattura", e);
        }
    }

    private String formatEuro(NumberFormat currency, double value) {
        String s = currency.format(value); // es: "24.999,99 €" o "24.999,99 €"
        s = s.replace('\u00A0', ' ').trim(); // rimuove eventuale NBSP
        if (s.endsWith("€")) {
            s = s.substring(0, s.lastIndexOf('€')).trim();
        }
        return "€ " + s;
    }
}