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
    if (!locacaoData) {
      functions.logger.error("addLocacao " +
          "- Erro ao inserir nova Pessoa: Dados não recebidos corretamente"),
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
        console.error("Erro ao obter referências à pessoa:", error);
        throw new functions.https.HttpsError("unknown",
          "Falha ao obter referências à pessoa", error);
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
