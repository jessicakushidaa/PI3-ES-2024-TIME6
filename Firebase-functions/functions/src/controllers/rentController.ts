/* eslint-disable linebreak-style */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";
// Retorno do método de admin - inicializa o app do firebase
const db = admin.firestore(); // chamada do banco de dados

// Referenciando a collection "locacao" do projeto firebase
const colLocacao = db.collection("locacao");

/** Função que adiciona a locação ao database
 * precisa passar como parâmetro/data:
 * id do documento da unidade de locacao
 * id do documento do cliente - chamar function getDocumentById
 * id do armario que está na unidade de loc - pegar pelo getDocumentFields
 (document)
 *
*/
export const addLocacao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    // Locação é adicionada como pendente - confirmada apenas com gerente
    const status = "pendente";

    // Extrair os dados recebidos do cliente
    const unidadeLocacao = data.idUnidade;

    const locacaoData = {
      armario: data.idArmario,
      cliente: data.idPessoa,
      precoTempoEscolhido: {
        tempo: data.tempoEscolhido,
        preco: data.precoEscolhido,
      },
      status: status,
    };

    functions.logger.info("Function addLocacao - Iniciada.");

    // Caso a request não conste os campos esperados
    if (!locacaoData || !unidadeLocacao) {
      functions.logger.error("addLocacao " +
          "- Erro ao inserir nova locacao: Dados não recebidos corretamente"),
      result = {
        status: "ERROR",
        message: "Parâmetros não recebidos corretamente",
        payload: JSON.parse(JSON.stringify({docId: null})),
      };
    }

    // Verificar se há armário na collection unidadeData
    /* A verificação da existência do armário é feita também na function
    getDocumentFields,aqui ela é feita para verificar o caminho coerente */
    if (locacaoData.armario && locacaoData.armario!=null) {
      try {
        // Extrair os dados recebidos do cliente
        const pathArmario =
         `unidadeLocacao/${data.idUnidade}/armarios/${data.idArmario}`;
        const armarioRef = db.doc(pathArmario);

        // Recuperar caminho do documento do cliente que alugou armário
        const clienteRefs = await Promise
          .all(locacaoData.cliente.map(async (pessoa: {path: string}) => {
            const clienteRef = admin.firestore().doc(`pessoas/${pessoa}`);
            const clienteSnapshot = await clienteRef.get();
            if (!clienteSnapshot.exists) {
              throw new Error(`Cliente não encontrado para o ID: ${pessoa}`);
            }
            return clienteRef;
          }));

        // Substituir o path do(s) cliente(s) pelas referências aos documentos
        locacaoData.cliente = clienteRefs;
        locacaoData.armario = armarioRef;
      } catch (error) {
        console.error("Erro ao obter referências à pessoa ou armário:", error);
        throw new functions.https.HttpsError("unknown",
          "Falha ao obter referências à pessoa e/ou armário", error);
      }
    }
    try {
      // Adicionar dados recebidos a um novo documento de locação
      const locDocRef = await colLocacao.add(locacaoData);

      // Resposta de Sucesso
      result = {
        status: "SUCCESS",
        message: "Locação - não confirmada - inserida com sucesso.",
        payload: JSON.parse(JSON.stringify({locId: locDocRef.id.toString()})),
      };
      functions.logger.info("addLocação - Nova locação (pendente) inserida");
    } catch (error) {
      // Lidar com erro
      functions.logger.error("Erro ao adicionar locação:", error);
      throw new functions.https.HttpsError("unknown",
        "Falha ao adicionar locação", error);
    }
    return result;
  });

export const checkLocacao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    const idPessoa = data.idPessoa;
    let result: CallableResponse;

    functions.logger.info("Function checkLocacao - Iniciada");
    try {
      const pessoaRef = db.doc(`pessoas/${idPessoa}`);
      const locSnapshot = await colLocacao
        .where("cliente", "array-contains", pessoaRef)
        .where("status", "==", "pendente").limit(1).get();
      const pendente = !locSnapshot.empty;
      if (pendente) {
        const locPendente =locSnapshot.docs[0];
        result = {
          status: "SUCCESS",
          message: "Locacao pendente encontrada.",
          payload: JSON.parse(JSON.stringify({locSnapshot:
            {idPessoa: idPessoa, pendente: pendente, idLocacao: locPendente.id,
              data: locPendente.data()}})),
        };
      } else {
        result = {
          status: "SUCCESS",
          message: "Não há nenhuma locação pendente",
          payload: JSON.parse(JSON.stringify({locSnapshot:
            {pendente: pendente}})),
        };
      }
    } catch (error: any) {
      // Lidar com erros
      result = {
        status: "ERROR",
        message: "Erro ao obter documentos: " + error.message,
        payload: JSON.parse(JSON.stringify({locSnapshot: null})),
      };
      functions.logger.error("checkLocacao " +
          "- Erro ao buscar documentos:" + error.message);
    }
    // Retorna a resposta
    return result;
  });
