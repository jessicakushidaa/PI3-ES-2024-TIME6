/* eslint-disable linebreak-style */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";

// retorno do método de admin - inicializa o app do firebase
const app = admin.initializeApp();
const db = app.firestore(); // chamada do banco de dados


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
