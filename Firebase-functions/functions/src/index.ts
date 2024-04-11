/* eslint-disable linebreak-style */
// importar as funcionalidades do firebase functions, apelidando de 'functions';
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";


// const app recebe o retorno do método "initializeApp" de admin
// inicializa o app do firebase
const app = admin.initializeApp();
const db = app.firestore(); // chamada do banco de dados
// collection do firestore, caso nao tenha, ele cria;
const colPessoas = db.collection("pessoas");

  /* padronizando o tipo da resposta à chamada da função */
  interface CallableResponse {
    status: string,
    message: string,
    payload: JSON
  }

  /* tipando os atributos do objeto que será inserido na collection */
  // campo "userId" vem como parametro no lado do cliente, id do usuário
  // autenticado
  interface Pessoa {
    userId: string,
    nome: string,
    sobrenome: string,
    dataNascimento: Date,
    cpf: string,
    telefone: string,
    isGerente: boolean
  }

/* Função que analisa se o formulário foi preenchido */
// *** obs: validar tamanho do telefone e cpf, tipo de caracter ***
function camposPreenchidos(p: Pessoa) {
  if (!p.userId) {
    return 6;
  }
  if (!p.nome) {
    return 1;
  }
  if (!p.sobrenome) {
    return 2;
  }
  if ((p.dataNascimento.getFullYear() < 1924) ||
  (p.dataNascimento.getFullYear() > 2006)) {
    return 3;
  }
  if (!p.cpf) {
    return 4;
  }
  if (!p.telefone) {
    return 5;
  } else return 0;
}

/*
  ** Função que analisa se os campos do formulário foram preenchidos com os
 tipos de dados corretos
  ** Retorna array com os campos incoerentes
*/
function validarTipos(p: Pessoa): string[] | null {
  const camposInvalidos: string[] = [];

  if (typeof p.nome !== "string") {
    camposInvalidos.push("nome");
  }
  if (typeof p.sobrenome !== "string") {
    camposInvalidos.push("sobrenome");
  }
  if (!(p.dataNascimento instanceof Date)) {
    camposInvalidos.push("dataNascimento");
  }
  if (typeof p.telefone !== "string") {
    camposInvalidos.push("telefone");
  }
  if (typeof p.isGerente !== "boolean" &&
   typeof p.isGerente !== "undefined") {
    camposInvalidos.push("isGerente");
  }
  if (camposInvalidos.length > 0) {
    return camposInvalidos;
  }
  return null;
}

/* Funcao que determina mensagem de erro da Response, caso campo vazio */
function mensagemErro(codigo: number) {
  let message = "";
  switch (codigo) {
  case 1: {
    message = "Nome do usuário não informado.";
    break;
  }
  case 2: {
    message = "Sobrenome do usuário não informado.";
    break;
  }
  case 3: {
    message = "Ano de nascimento inválido.";
    break;
  }
  case 4: {
    message = "CPF do usuário não informado.";
    break;
  }
  case 5: {
    message = "Número de telefone do usuário não informado.";
    break;
  }
  case 6: {
    message = "Usuário não autenticado";
    break;
  }
  }
  return message;
}
/* Função para adicionar dados da pessoa cadastrada ao firebase database */
export const addPessoa = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    functions.logger.info("Function addPessoa - Iniciada.");

    // Criando objeto pessoa com os dados recebidos - baseando nos parametros
    const pessoa: Pessoa = {
      userId: data.userId,
      nome: data.nome,
      sobrenome: data.sobrenome,
      dataNascimento: new Date(data.dataNascimento),
      cpf: data.cpf,
      telefone: data.telefone,
      isGerente: data.isGerente,
    };

    // Verificar se os tipos dos campos de pessoa correspondem à interface
    const erroCodigo = camposPreenchidos(pessoa);
    const erroMensagem = mensagemErro(erroCodigo);
    const camposInvalidos = validarTipos(pessoa);

    if (erroCodigo > 0) {
      // gravar o erro no log e preparar o retorno.
      functions.logger.error("addPessoa " +
        "- Erro ao inserir nova Pessoa:" +
        erroCodigo.toString()),

      result = {
        status: "ERROR",
        message: erroMensagem,
        payload: JSON.parse(JSON.stringify({docId: null})),
      };
      console.log(result);
    } else if (camposInvalidos !== null && camposInvalidos.length > 0) {
      result = {
        status: "ERROR",
        message: `Os seguintes campos não correspondem ao tipo esperado:
         ${camposInvalidos.join(", ")}.`,
        payload: JSON.parse(JSON.stringify({docId: null})),
      };
    } else {
      // cadastrar a Pessoa pois está ok.
      const docRef = await colPessoas.add(pessoa);
      // await docRef.update({idPessoa: docRef.id});
      result = {
        status: "SUCCESS",
        message: "Pessoa inserida com sucesso.",
        payload: JSON.parse(JSON.stringify({docId: docRef.id.toString()})),
      };
      functions.logger.error("addPessoaTeste - Nova pessoa inserida");
    }

    // Retornando o objeto result.
    return result;
  });

interface Cartao {
  nomeTitular: string;
  numeroCartao: string;
  dataVal: Date;
}

function camposPreenchidosCartao(cartao: Cartao): number {
  if (!cartao.nomeTitular) {
    return 1;
  }
  if (!cartao.numeroCartao) {
    return 2;
  }
  if (!cartao.dataVal) {
    return 3;
  } else return 0;
}

function validarTiposCartao(cartao: Cartao): string[] | null {
  const camposInvalidos: string[] = [];

  if (typeof cartao.nomeTitular !== "string") {
    camposInvalidos.push("nomeTitular");
  }
  if (typeof cartao.numeroCartao !== "string") {
    camposInvalidos.push("numeroCartao");
  }
  if (!(cartao.dataVal instanceof Date)) {
    camposInvalidos.push("dataVal");
  }

  return camposInvalidos.length > 0 ? camposInvalidos : null;
}

function errorMessage(codigo: number): string {
  switch (codigo) {
  case 1:
    return "Nome do titular do cartão não informado.";
  case 2:
    return "Número do cartão não informado.";
  case 3:
    return "Data de validade não informado";
  default:
    return "";
  }
}

export const addCartao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    functions.logger.info("Function addCartao - Iniciada.");

    const cartao: Cartao = {
      nomeTitular: data.nomeTitular,
      numeroCartao: data.numeroCartao,
      dataVal: new Date(data.dataVal),
    };

    const codigoErro = camposPreenchidosCartao(cartao);
    const mensagemErro = errorMessage(codigoErro);
    const camposInvalidos = validarTiposCartao(cartao);

    // Validação dos dados recebidos
    if (codigoErro > 0) {
      // gravar o erro no log e preparar o retorno.
      functions.logger.error("addCartao " +
              "- Erro ao inserir novo Cartao:" +
              codigoErro.toString()),

      result = {
        status: "ERROR",
        message: mensagemErro,
        payload: JSON.parse(JSON.stringify({docId: null})),
      };
      console.log(result);
    } else if (camposInvalidos !== null && camposInvalidos.length > 0) {
      result = {
        status: "ERROR",
        message: `Os seguintes campos não correspondem ao tipo esperado:
               ${camposInvalidos.join(", ")}.`,
        payload: JSON.parse(JSON.stringify({docId: null})),
      };
    } else {
      try {
        // Obter o ID do documento da pessoa
        const idPessoa: string = data.idPessoa;

        // Referencia a subcolletion criada dentro do doc da Pessoa
        const subcolCartoes= db.collection(`pessoas/${idPessoa}/cartoes`);

        // Adicionando o novo cartão
        const docRef = await subcolCartoes.add(cartao);

        // Resposta de sucesso
        result = {
          status: "SUCCESS",
          message: "Cartao inserido com sucesso.",
          payload: JSON.parse(JSON.stringify({docId: docRef.id.toString()})),
        };
        functions.logger.error("addCartao - Novo cartão inserido");
      } catch (error:any) {
        result = {
          status: "ERROR",
          message: "Erro ao adicionar cartão: " + error.message,
          payload: JSON.parse(JSON.stringify({docId: null}))};
      }
    }
    return result;
  });

/* Função que retorna campos do documento solicitado do bd */
export const getDocumentById = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;
    try {
      // Declarando as constantes dos parâmetros recebidos:
      // ID do documento e nome da coleção Firebase
      const documentId: string = data.documentId;
      const collectionName: string = data.collectionName;

      // Atribuindo as referências da coleção e do documento ID do Firebase
      const collectionRef = db.collection(collectionName);
      const docRef = collectionRef.doc(documentId);

      // Recupera o documento que tem o tal ID da coleção
      const documentSnapshot = await docRef.get();

      // Verifica se o documento existe
      if (!documentSnapshot.exists) {
        // Se o documento não existir, retorna mensagem de erro
        result = {
          status: "ERRO",
          message: "Documento não existe",
          payload: JSON.parse(JSON.stringify({documentSnapshot: null})),
        };
      } else {
        // Sucesso: retorna os dados do documento
        result = {
          status: "SUCCESS",
          message: "Documento recuperado com sucesso",
          payload: JSON.parse(JSON
            .stringify({documentSnapshot: documentSnapshot.data()})),
        };
        functions.logger.info("getDocumentById - documento recuperado");
      }
    } catch (error: any) {
      // Lidar com erros
      result = {
        status: "ERROR",
        message: "Erro ao obter documento: " + error.message,
        payload: JSON.parse(JSON.stringify({documentSnapshot: null})),
      };
      functions.logger.error("getElementById " +
        "- Erro ao buscar documento:" + error.message);
    }
    // Retorna a resposta
    return result;
  });
