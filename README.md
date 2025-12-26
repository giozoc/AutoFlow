# AutoFlow
[![Swagger](https://img.shields.io/badge/Swagger-Online-brightgreen)](https://localhost:8080/swagger-ui/index.html)
[![Architettura](https://img.shields.io/badge/Architecture-Three--Tier-blue)](#architettura)
[![Licenza](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](#licenza)
[![GitHub Release](https://img.shields.io/github/v/release/giozoc/AutoFlow)](https://github.com/giozoc/AutoFlow/releases)

<div align="center">
  <img src="Deliverables/logo.png" alt="AutoFlow Logo" width="200" height="200">
</div>

---

## Documentazione

AutoFlow fornisce una documentazione completa sia a livello di API che di progettazione del sistema.

- **Swagger API Documentation**  
  Documentazione interattiva delle API REST, utilizzabile per esplorare e testare gli endpoint.  
  👉 https://localhost:8080/swagger-ui/index.html

- **Documentazione di progetto (Deliverables)**:
  - Requirements Analysis Document (RAD)
  - System Design Document (SDD)
  - Object Design Document (ODD)
  - Test Plan, Test Case Specification e Test Report

---

## Panoramica del Progetto

AutoFlow è una piattaforma web progettata per **digitalizzare e centralizzare** i processi operativi di concessionarie automobilistiche di piccole e medie dimensioni, offrendo:

- gestione dello showroom (fisico e virtuale)
- configurazione dei veicoli e creazione di proposte commerciali
- tracciamento del flusso di vendita
- generazione e archiviazione di documenti PDF (preventivi e fatture)
- gestione degli accessi basata sui ruoli (admin, venditore, cliente)

---

## Stack Tecnologico

- **Backend**: Java 25 + Spring Boot (REST)
- **Frontend**: React + TypeScript
- **Database**: MariaDB / MySQL
- **Sicurezza**: HTTPS (TLS) e controllo degli accessi basato sui ruoli
- **Documentazione API**: OpenAPI / Swagger

---

## Architettura

AutoFlow adotta un’**architettura three-tier**, suddivisa nei seguenti livelli:

1. **Presentation Layer** – interfaccia utente web
2. **Application Layer** – logica applicativa (Controller e Service)
3. **Data Management Layer** – gestione dei dati persistenti e dei file PDF

---

## Avvio dell’Applicazione

Il backend viene normalmente **avviato tramite IntelliJ IDEA**, eseguendo la classe principale Spring Boot.  
Maven viene utilizzato per la build e l’esecuzione dei test.

---

## Licenza

Questo progetto è distribuito sotto licenza **GNU GPL v3.0**.
