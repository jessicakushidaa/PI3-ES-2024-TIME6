/* eslint-disable linebreak-style */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";

// retorno do método de admin - inicializa o app do firebase
const db = admin.firestore(); // chamada do banco de dados

// incializando collection Pessoas no firestore db
const colPessoas = db.collection("pessoas");

export const getDocumentId = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    functions.logger.info("Function getDocumentId - Iniciada.");
    try {
      // Verificar se o usuário está autenticado
      if (!context.auth) {
        throw new functions.https
          .HttpsError("unauthenticated",
            "O usuário deve estar autenticado para acessar o ID do documento.");
      }

      // Extrair o UID do usuário autenticado
      const userId = context.auth.uid;

      // Recupera o documento que tem o tal ID da coleção
      const documentSnapshot = await colPessoas.where("userId", "==", userId)
        .get();

      // Verificar se o documento existe
      if (documentSnapshot.empty) {
        // Se o documento não existir, retorna mensagem de erro
        result = {
          status: "ERRO",
          message: "Nenhum documento encontrado para o id auth fornecido.",
          payload: JSON.parse(JSON.stringify({documentSnapshot: null})),
        };
      } else {
        // Sucesso: retorna os dados do documento
        const documentId = documentSnapshot.docs[0].id;
        result = {
          status: "SUCCESS",
          message: "Documento recuperado com sucesso",
          payload: JSON.parse(JSON.stringify({documentId: documentId})),
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


/**
 *  Função que retorna todos os campos do documento solicitado do bd
 * @param {string} documentId - Id do documento no firebase.
 * @param {string} collectionName - Coleção onde o documento está armazenado.
 * @returns {JSON} campos do documento (mainData e subCollectionsData).
*/
export const getDocumentFields = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    functions.logger.info("Function getDocumentFields - Iniciada.");
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

      // Verificar se o documento existe
      if (!documentSnapshot.exists) {
        // Se o documento não existir, retorna mensagem de erro
        result = {
          status: "ERRO",
          message: "Documento não existe",
          payload: JSON.parse(JSON.stringify({documentSnapshot: null})),
        };
      } else {
        // Separar dados entre Campos Principais e Campos da Subcoleção Firebase
        const mainData = documentSnapshot.data();

        /* Obter todas as subcoleções e os dados de seus docs em paralelo
           ** retorna uma promise que resolve p/ um array com as subcollections
            do documento
           ** itera sobre a lista de subcollections
           ** retorna array de promises com nome, dados dos documentos
        */
        const subCollections = await docRef.listCollections();
        const subCollectionsDataPromises = subCollections
          .map(async (subCollection) => {
            const subCollectionName = subCollection.id;
            const subCollectionSnapshot = await subCollection.get();
            const subCollectionDocsData = subCollectionSnapshot.docs
              .map((doc) => doc.data());
            return {[subCollectionName]: subCollectionDocsData};
          });

        // Esperar que todas as Promises sejam resolvidas
        const subCollectionsData= await Promise.all(subCollectionsDataPromises);

        // Sucesso: retorna os dados do documento
        result = {
          status: "SUCCESS",
          message: "Documento recuperado com sucesso",
          payload: JSON.parse(JSON.stringify({
            mainData: mainData,
            subCollectionsData: Object.assign({}, ...subCollectionsData),
          })),
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
