/* eslint-disable linebreak-style */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";
import {UnidadeLocacao} from "../models/rentalUnit";

// Retorno do método de admin - inicializa o app do firebase
const db = admin.firestore(); // chamada do banco de dados

// Referenciando a collection UnidadeLocacao do projeto firebase
export const colUnidades = db.collection("unidadeLocacao");

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

/* Função {opcional} para adicionar unidades de locação */
export const addUnidadeLocacao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    // Extrair os dados recebidos do cliente
    const {unidadeData, count} = data;

    if (!unidadeData || !count ) {
      throw new functions.https.HttpsError("invalid-argument",
        "Os parâmetros unidadeData, count e  são obrigatórios");
    }

    // Verificar se há gerentes (paths) sendo passados na unidadeData
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
      return await addArmarios({unitId: unitDocRef.id, count});
    } catch (error) {
      console.error("Erro ao adicionar unidade e armários:", error);
      throw new functions.https.HttpsError("unknown",
        "Falha ao adicionar unidade e armários", error);
    }
  });

async function addArmarios(data:
    {unitId: string, count: number}) {
  const {unitId, count} = data;
  const db = admin.firestore();
  const unitRef = colUnidades.doc(unitId);
  const lockersRef = unitRef.collection("armarios");

  const batch = db.batch();
  for (let i = 1; i <= count; i++) {
    const lockerTag = `loc-${String(i).padStart(3, "0")}`;
    const lockerDoc = lockersRef.doc();
    batch.set(lockerDoc, {
      tag: lockerTag,
    });
  }

  await batch.commit();
  return {result: `Sucesso! ${count} armários adicionados em ${unitId}.`};
}


