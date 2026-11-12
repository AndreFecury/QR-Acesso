<div align="center">

<img src="https://raw.githubusercontent.com/AndreFecury/AndreFecury/main/assets/qracesso.svg" width="480" alt="QR Acesso: app Android que guarda o QR Code da catraca da faculdade" />

# QR Acesso

O QR Code da catraca da faculdade em tela cheia, assim que você abre o app. Sem login, sem menus e sem internet.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![ML Kit](https://img.shields.io/badge/ML_Kit-FF6F00?style=for-the-badge&logo=google&logoColor=white)

</div>

## Sobre

O **QR Acesso** guarda no celular o QR Code da catraca da sua instituição de ensino e o mostra assim que você abre o app.

Você importa uma vez o print da carteirinha digital, o app encontra o QR Code na imagem, recorta e salva só ele. Depois disso, é só tocar no ícone: o código aparece em tela cheia, sem login, sem menus e sem internet.

O app não gera nem altera QR Codes: só guarda e exibe a imagem que você já possui.

## Funcionalidades

| | |
|---|---|
| **Importação inteligente** | Escolha o print na galeria e o app detecta e recorta o QR Code automaticamente com o ML Kit, offline |
| **Abre direto no código** | A tela principal é o próprio QR Code, sem splash, login ou menus |
| **Leitura fácil na catraca** | Mantém a tela ligada e aumenta o brilho enquanto o código está na tela, voltando ao normal quando você sai |
| **Troca segura** | Ao importar um novo print, o código antigo só é substituído se o novo for válido |
| **Vários códigos na imagem** | Se o print tiver mais de um QR Code, o app pede para você escolher o certo |
| **Semestre** | Anote o período do código (ex.: 2026/2) nas Configurações |
| **Atalho e widget** | Atalho no ícone do app ("Mostrar QR Code") e widget na tela inicial |

## Privacidade

- Tudo fica armazenado localmente: sem servidor, sem conta, sem anúncios e sem analytics.
- **Nenhuma permissão perigosa** no manifesto. A imagem é escolhida pelo Photo Picker do próprio Android.
- O QR Code é salvo no armazenamento interno e privado do app e nunca aparece nos logs.
- A gravação é atômica (arquivo temporário e depois `rename`), o que evita corromper o código salvo.
- O código fica fora do backup em nuvem, mas é incluído na transferência direta entre aparelhos.

## Arquitetura

```
app/src/main/kotlin/com/qracesso/app/
├── domain/         # contratos (repositório, detector, carregador de imagem) e estados do QR Code
├── data/           # QrCodeStorage (PNG + SharedPreferences), detecção com ML Kit
├── presentation/   # telas em Jetpack Compose, MainViewModel e navegação
├── util/           # tela sempre ligada, controle de brilho e leitura de Bitmap
└── widget/         # widget da tela inicial (AppWidgetProvider)
```

As camadas dependem das interfaces do `domain`, não das implementações do `data`. Assim o `MainViewModel` é testado inteiro com dublês, sem arquivo, ML Kit ou `ContentResolver` reais.

A tela principal é uma máquina de estados (`NO_QR_CODE`, `IMPORTING`, `PROCESSING`, `QR_CODE_READY`, `INVALID_IMAGE`, `MULTIPLE_QR_CODES`) exposta por um `StateFlow` e desenhada com `when` no Compose.

## Decisões técnicas

- **ML Kit com modelo embutido no app** (`barcode-scanning`) em vez da versão que baixa o modelo pelo Google Play services. O APK fica alguns MB maior, mas a detecção funciona offline desde o primeiro uso.
- **Brilho pela janela do app** (`WindowManager.LayoutParams.screenBrightness`), sem a permissão `WRITE_SETTINGS`. O brilho só muda enquanto o app está aberto.
- **Ícones vetoriais próprios** em vez da biblioteca `material-icons-extended`, que adicionaria vários MB por cinco ícones.
- **Sem framework de injeção de dependência.** `QRAcessoApplication` monta as três dependências manualmente.

## Tecnologias

| | |
|---|---|
| Linguagem | Kotlin |
| Interface | Jetpack Compose + Material 3 |
| Detecção de QR Code | Google ML Kit Barcode Scanning |
| Navegação | Navigation Compose |
| Build | Gradle Kotlin DSL + catálogo de versões |
| Testes | JUnit 4, Robolectric, Compose UI Test |
| Compatibilidade | Android 8.0+ (minSdk 26, targetSdk 36) |

## Como compilar

1. Abra a pasta do projeto no **Android Studio**.
2. Deixe o Gradle sincronizar. O Android Studio gera o Gradle Wrapper automaticamente se ele estiver faltando.
3. Rode em um aparelho ou emulador com Android 8.0 ou superior.

## Testes

```bash
./gradlew testDebugUnitTest          # unidade: repositório e todas as transições do MainViewModel
./gradlew connectedDebugAndroidTest  # interface: estado vazio da tela principal (requer aparelho/emulador)
```

---

<div align="center"><sub>Feito por <a href="https://github.com/AndreFecury">André Fecury</a> · Manaus, AM</sub></div>
