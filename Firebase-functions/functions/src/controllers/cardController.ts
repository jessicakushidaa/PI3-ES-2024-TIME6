/* eslint-disable linebreak-style */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";
import {Cartao} from "../models/card";

// retorno do método de admin - inicializa o app do firebase
const db = admin.firestore(); // chamada do banco de dados


// Verifica se os campos obrigatórios do cartão estão preenchidos
function camposPreenchidosCartao(cartao: Cartao): number {
  if (!cartao.nomeTitular) {
    return 1; // Código de erro: Nome do titular não informado
  }
  if (!cartao.numeroCartao) {
    return 2; // Código de erro: Número do cartão não informado
  }
  if (!cartao.dataVal) {
    return 3; // Código de erro: Data de validade não informada
  } else return 0; // Todos os campos estão preenchidos
}

/*
    ** Função que analisa se os campos do formulário foram preenchidos com os
   tipos de dados corretos
    ** Retorna array com os campos incoerentes
  */
function validarTiposCartao(cartao: Cartao): string[] | null {
  const camposInvalidos: string[] = [];

  if (typeof cartao.nomeTitular !== "string") {
    camposInvalidos.push("nomeTitular");
  }
  if (typeof cartao.numeroCartao !== "string") {
    camposInvalidos.push("numeroCartao");
  }
  if (!(cartao.dataVal !== "string")) {
    camposInvalidos.push("dataVal");
  }
  // Retorna campos inválidos ou null se tudo estiver certo
  return camposInvalidos.length > 0 ? camposInvalidos : null;
}

/* Retorna a mensagem de erro correspondente ao código
   retornado de CamposPreenchidosCartao */
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

// Função Firebase Cloud para adicionar um novo cartão
export const addCartao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, _context) => {
    let result: CallableResponse; // Define a resposta da função

    functions.logger.info("Function addCartao - Iniciada.");

    // Cria um objeto cartão com os dados recebidos
    const cartao: Cartao = {
      nomeTitular: data.nomeTitular,
      numeroCartao: data.numeroCartao,
      dataVal: data.dataVal,
    };

    // Verifica se os campos obrigatórios estão preenchidos
    const codigoErro = camposPreenchidosCartao(cartao);
    const mensagemErro = errorMessage(codigoErro);

    // Valida os tipos dos campos do cartão
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
        functions.logger.info("addCartao - Novo cartão inserido");
      } catch (error:any) {
        result = {
          status: "ERROR",
          message: "Erro ao adicionar cartão: " + error.message,
          payload: JSON.parse(JSON.stringify({docId: null}))};
      }
    }
    return result;
  });
