/* eslint-disable linebreak-style */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";
import {UnidadeLocacao} from "../models/rentalUnit";

// Retorno do método de admin - inicializa o app do firebase
const db = admin.firestore(); // chamada do banco de dados

// Referenciando a collection UnidadeLocacao do projeto firebase
const colUnidades = db.collection("unidadeLocacao");

/* Função que retorna todos os docs de Unidades de Locação da collection */
export const getAllUnits = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    functions.logger.info("Function getAllUnits - Iniciada.");
    try {
      // Recupera todos os documentos da collection
      const querySnapshot = await colUnidades.get();

      // Verificar se existe pelo menos um documento na collection
      if (querySnapshot.empty) {
        result = {
          status: "ERRO",
          message: "Nenhuma unidade de locação no banco de dados.",
          payload: JSON.parse(JSON.stringify({querySnapshot: null})),
        };
      } else {
        /**
         * Mapear os documentos retornados para objetos que correspondam à
         interface UnidadeLocacao.
         * O campo "coordenadas" é um geopoint ->
         {latitude: number, longitude: number}
         */
        const unidades: UnidadeLocacao[] = querySnapshot.docs.map((doc) => {
          const unidadeData = doc.data();
          return {
            id: doc.id,
            nome: unidadeData.nome,
            coordenadas: unidadeData.coordenadas,
            endereco: unidadeData.endereco,
            descricao: unidadeData.descricao,
            gerentes: unidadeData.gerentes,
            telefone: unidadeData.telefone,
            armarios: unidadeData.armarios,
            tabelaPrecos: unidadeData.tabelaPrecos,
          };
        });

        result = {
          status: "SUCCESS",
          message: "Unidades de locação recuperadas com sucesso",
          payload: JSON.parse(JSON.stringify({unidades: unidades})),
        };
        functions.logger.info("getAllUnits - documentos recuperados");
      }
    } catch (error: any) {
      // Lidar com erros
      result = {
        status: "ERROR",
        message: "Erro ao obter documentos: " + error.message,
        payload: JSON.parse(JSON.stringify({documentSnapshot: null})),
      };
      functions.logger.error("getAllUnits " +
          "- Erro ao buscar documentos:" + error.message);
    }
    // Retorna a resposta
    return result;
  });

/* Função (opcional) que adiciona Unidade de Locação */
/* export const addUnidadeLocacao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    try {
    // Extrair os dados recebidos do cliente
      const unidadeData = data.unidade;
      const armariosData = data.armarios;

      // Verificar se os dados da unidade foram fornecidos
      if (!unidadeData) {
        throw new functions.https.HttpsError("invalid-argument",
          "Os dados da unidade de locação são obrigatórios.");
      }

      // Adicionar a unidade de locação à coleção
      const docRef = await colUnidades.add(unidadeData);

      // Adicionar os armários como documentos à subcoleção "armarios" da unidde
      const armariosRefPromises = armariosData.map(async (armario:
        admin.firestore.WithFieldValue<admin.firestore.DocumentData>) => {
        await docRef.collection("armarios").add(armario);
      });
      await Promise.all(armariosRefPromises);

      // Retorna o ID do documento recém-adicionado
      result = {
        status: "SUCCESS",
        message: "Unidade inserida com sucesso.",
        payload: JSON.parse(JSON.stringify({id: docRef.id.toString()})),
      };
    } catch (error: any) {
    // Em caso de erro, lançar uma exceção para ser tratada pelo cliente
      console.error("Erro ao adicionar unidade de locação:", error);
      result = {
        status: "ERROR",
        message: "Erro ao obter documentos: " + error.message,
        payload: JSON.parse(JSON.stringify({id: null})),
      };
    }
    return result;
  }); */

export const addUnidadeLocacao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    // Extrair os dados recebidos do cliente
    const {unidadeData, count, situation} = data;

    if (!unidadeData || !count || !situation) {
      throw new functions.https.HttpsError("invalid-argument",
        "Os parâmetros unidadeData, count e situation são obrigatórios");
    }

    // Verificar se há gerentes na unidadeData
    if (unidadeData.gerentes && Array.isArray(unidadeData.gerentes)) {
      try {
      // Mapear os paths dos gerentes para referências aos documentos
        const gerentesRefs = await Promise
          .all(unidadeData.gerentes.map(async (gerente: { path: string; }) => {
            const gerenteRef = admin.firestore().doc(gerente.path);
            const gerenteSnapshot = await gerenteRef.get();
            if (!gerenteSnapshot.exists) {
              throw new Error(`Gerente não encontrado para o caminho:
               ${gerente.path}`);
            }
            return gerenteRef;
          }));

        // Substituir os paths dos gerentes pelas referências aos documentos
        unidadeData.gerentes = gerentesRefs;
      } catch (error) {
        console.error("Erro ao obter referências aos gerentes:", error);
        throw new functions.https.HttpsError("unknown",
          "Falha ao obter referências aos gerentes", error);
      }
    }
    try {
      const unitDocRef = await db.collection("unidadeLocacao")
        .add(unidadeData);
      return await addArmarios({unitId: unitDocRef.id, count, situation});
    } catch (error) {
      console.error("Erro ao adicionar unidade e armários:", error);
      throw new functions.https.HttpsError("unknown",
        "Falha ao adicionar unidade e armários", error);
    }
  });

async function addArmarios(data:
    {unitId: string, count: number, situation: string}) {
  const {unitId, count, situation} = data;
  const db = admin.firestore();
  const unitRef = colUnidades.doc(unitId);
  const lockersRef = unitRef.collection("armarios");

  const batch = db.batch();
  for (let i = 1; i <= count; i++) {
    const lockerId = `loc-${String(i).padStart(3, "0")}`;
    const lockerDoc = lockersRef.doc(lockerId);
    batch.set(lockerDoc, {
      tag: lockerId,
      situacao: situation,
    });
  }

  await batch.commit();
  return {result: `Sucesso! ${count} armários adicionados em ${unitId}.`};
}


