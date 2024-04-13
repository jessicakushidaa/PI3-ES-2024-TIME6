/* eslint-disable linebreak-style */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";
import {Pessoa} from "../models/user";

// retorno do método de admin - inicializa o app do firebase
const app = admin.initializeApp();
const db = app.firestore(); // chamada do banco de dados

// incializando collection Pessoas no firestore db
const colPessoas = db.collection("pessoas");

/* VALIDAÇÃO */
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
/* FIREBASE- FUNCTION */
/* Função para adicionar dados da pessoa cadastrada ao firebase database */
export const addPessoa = functions
  .region("southamerica-east1")
  .https.onCall(async (data, _context) => {
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
