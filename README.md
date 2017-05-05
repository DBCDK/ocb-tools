<img src="http://www.dbc.dk/logo.png" alt="DBC" title="DBC" align="right">

# OCB-Tools

OCB-Tools er et sæt tools og libraries som har til formål at effektivisere arbejdet med Opencat-business modulet.

Modulet skal kunne udføres på følgende platforme:

- Windows.
- Linux.
- Mac OS X.

Skabeloner:

- Sikring af syntaks.
- Sikring af valideringsregler og konfiguration af deres parametre.
- Sikring at en skabelon kan validere bestemte poster korrekt. F.eks. at en bog-skabelon faktisk kan validere
forskellige bøger korrekt.

JavaScript:

- Eksekvering af JavaScript's unittests.

## OCB Kommandoer


OCB-Tools består af en række kommandolinjeværktøjer som kan anvendes til det daglige arbejde med Opencat-business.

Nedenstående

- **ocb-tt**: Udførelse af tests for skabeloner.
- **ocb-record**: CRUD af poster som skal bruges i testcases.
- **ocb-template**: CRUD af skabeloner.

Databaser:

- **ocb-rr**: CRUD af råpostdatabasen.
- **ocb-holdings**: CRUD af holdingsdatabasen.

Webservices:

- **ocb-os**: Kommando til at kalde OpenSearch.
- **ocb-forsrights**: Kommando til at kalde Forsrights.
- **ocb-fbs**: Kommando til at kalde Build eller Update.

## OCB Libraries


OCB Libraries har til formål at sikre at kode som anvendes op tværs af OCB kommandoer eller Opencat-services (Update og
Build) anvendes på samme måde.

Følgende libraries bruges af både OCB-Tools og Opencat-services:

- **ocb-scripter**: Library til at embedde det JavaScript som er i Opencat-business.

