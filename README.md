# GUARDIAN LOCKER APP

Este é o repositório do **Guardian Locker App**, que consiste em duas partes principais: a parte Android Studio e as funções do Firebase.

## Android Studio

A pasta `AndroidStudio` contém todo o código-fonte e recursos relacionados ao aplicativo Android desenvolvido no Android Studio.

### Estrutura de Pastas

- **app**: Contém o código-fonte principal do aplicativo, incluindo atividades, fragmentos, adaptadores e outros componentes.
- **res**: Contém todos os recursos do aplicativo, como layouts XML, arquivos de recursos, imagens, ícones, etc.
- **drawable**: Contém os arquivos de drawable utilizados para personalizar a interface do usuário.
- **values**: Contém arquivos XML para definição de cores, estilos, strings e outros recursos.
- **manifests**: Contém o arquivo AndroidManifest.xml, que descreve a estrutura e as permissões do aplicativo.

## Firebase Functions

A pasta `Firebase-functions` contém as funções Firebase escritas em TypeScript.

### Estrutura de Pastas

- **functions**: Contém o código-fonte das funções Firebase. Dividido entre: lib, node_modules, src
     - **src**: contém o código typescript do projeto com as functions e interfaces. Dividido entre: controllers e models.
     - **node_modules**: Contém as dependências do projeto. Instalação necessária para rodar o projeto.

## Como Utilizar

### Android Studio

1. Abra o projeto no Android Studio.
2. Faça as modificações necessárias no código-fonte e nos recursos.
     - abra o arquivo local.properties na visão de Projeto e adicione a referência à API do mapa: "MAPS_API_KEY = AIzaSyAol2dJabESlpiblrTmbN6XHeg8MyOKREM"
3. Execute o aplicativo em um emulador ou dispositivo Android.

### Firebase Functions

1. Instale as dependências do projeto executando `npm install`. Utilize esses comandos no seu terminal, no diretório `functions`:
     - `npm i typescript`
     - `npm run build`
     - `npm i firebase-functions firebase-admin`
3. Faça as modificações necessárias nas funções Firebase no diretório `functions`.
4. Implante as funções no Firebase executando `firebase deploy`.
