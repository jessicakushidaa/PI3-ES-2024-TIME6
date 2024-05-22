/* eslint-disable linebreak-style */
/* eslint-disable valid-jsdoc */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {CallableResponse} from "../models/customResponse";

// Retorno do método de admin - inicializa o app do firebase
const db = admin.firestore(); // chamada do banco de dados

// Referenciando a collection "locacao" do projeto firebase
const colLocacao = db.collection("locacao");
// Referenciando a collection "unidadeLocacao" do projeto firebase
const colUnidades = db.collection("unidadeLocacao");


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

    try {
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
    } catch (error) {
      console.error("Erro ao obter referências à pessoa ou armário:", error);
      throw new functions.https.HttpsError("unknown",
        "Falha ao obter referências à pessoa e/ou armário", error);
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

/**
 * Função que checa se a pessoa tem uma locação no banco de dados e a
  situação da locação - pendente ou não
 * Parâmetro passado na requisição pelo cliente:
 * @param idPessoa - id do documento da pessoa na collection
 * @returns - Retorna id da Locação, status e outros campos do documento
 */
export const checkLocacao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    // Extrair os dados recebidos do cliente
    const idPessoa = data.idPessoa;

    functions.logger.info("Function checkLocacao - Iniciada");
    try {
      // Referenciar documento da pessoa - path
      const pessoaRef = db.doc(`pessoas/${idPessoa}`);

      // Buscar locação onde haja o cliente no array de clientes
      // e na qual o status seja pendente (retorna a primeira que encontrar)
      const locSnapshot = await colLocacao
        .where("cliente", "array-contains", pessoaRef)
        .where("status", "==", "pendente").limit(1).get();
      const pendente = !locSnapshot.empty;
      /**
       * Verificar se a busca retornou vazia
       * Se não, atribui os dados do documento e retorna pendente = true com
       * os dados
       * Se sim, retorna pendente = false e array vazio
       **/
      if (pendente) {
        const locPendente =locSnapshot.docs[0];
        result = {
          status: "SUCCESS",
          message: "Locacao pendente encontrada.",
          payload: JSON.parse(JSON.stringify({idPessoa: idPessoa,
            pendente: pendente,
            locSnapshot: {idLocacao: locPendente.id,
              data: locPendente.data()}})),
        };
      } else {
        result = {
          status: "SUCCESS",
          message: "Não há nenhuma locação pendente",
          payload: JSON.parse(JSON.stringify({pendente: pendente,
            locSnapshot: []})),
        };
      }
      functions.logger.info("Busca realizada com sucesso." + result);
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


/**
   * Função que atualiza o status da locação para confirmada a partir da ação
   *  do gerente. Chamada após a checkLoc.
   *  Chama função de vincular um armário com status disponível à locação
   *  Adiciona vetor de strings de foto(s) do(s) usuário(s) com acesso
   *  ao armário ao banco
   *  Adiciona vetor de strings com a(s) tag(s) do(s) usuário(s) ao banco
   * @param {string} idLocacao - id do documento da locacao que está pendente
   *  no firebase.
   * @param {string[]} idTag - vetor de id's da(s) pulseira(s) que
   *  serão vinculadas à loc
   * @param {string[]} foto - vetor com bitmaps da(s) foto(s) dos clientes
   * @param {string} idUnidade - id da unidade de locação -> buscar armários
   *  disponíveis
   */
export const confirmarLoc = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    // Extrair os dados recebidos do cliente
    const {idLocacao, idTag, foto, idUnidade} = data;

    functions.logger.info("Function confirmarLoc - iniciada.");

    // Caso a request não conste os campos esperados
    if (!idLocacao || !idTag || !foto || !idUnidade) {
      functions.logger.error("confirmarLoc " +
        "- Erro ao confirmar locacao: Dados não recebidos corretamente"),
      result = {
        status: "ERROR",
        message: "Parâmetros não recebidos corretamente",
        payload: JSON.parse(JSON.stringify({docId: null})),
      };
    }
    try {
      // referencia ao documento de locação
      const locRef= colLocacao.doc(idLocacao);

      if (locRef != null) {
        const res = await alugarArmario(idUnidade);

        if (res.status === "SUCCESS") {
          const idArmario = res.payload.idArmario;

          // Criando campo "armario" que faz referencia ao doc do
          // armário da unidade
          const pathArmario =
         `unidadeLocacao/${data.idUnidade}/armarios/${idArmario}`;
          const armarioRef = db.doc(pathArmario);

          // Adicionando novos campos ao documento e atualizando status da loc
          locRef.set({
            armario: armarioRef,
            tags: idTag,
            foto: foto,
            status: "confirmada",
          }, {merge: true});

          result = {
            status: "SUCCESS",
            message: "Novos campos inseridos",
            payload: JSON.parse(JSON.stringify({
              idLocacao: locRef,
              data: {
                armario: armarioRef,
                tags: idTag,
                foto: foto,
              }})),
          };
        } else {
          // Caso erro na chamada de atualizarArmario()
          functions.logger.error("Erro:", res.message);
          result = {
            status: "ERROR",
            message: res.message,
            payload: JSON.parse(JSON.stringify({
              idLocacao: idLocacao,
              data: null,
            })),
          };
          return result;
        }
      } else {
        // Caso erro ao localizar documento de locação
        result = {
          status: "ERROR",
          message: "Locacao pendente nao existe",
          payload: JSON.parse(JSON.stringify({
            idLocacao: idLocacao,
            data: null,
          })),
        };
      }
    } catch (error: any) {
      functions.logger.error("confirmarLoc " +
      "- Erro ao confirmar locacao:" + error.message);

      result = {
        status: "ERROR",
        message: "Não foi possivel adicionar campos - erro inesperado",
        payload: JSON.parse(JSON.stringify({
          idLocacao: idLocacao,
          data: null,
        })),
      };
    }
    return result;
  });


/**
 * Função que busca se há armários disponiveis nessa unidade e, caso haja,
 * retorna id do primeiro disponível, atualizando o seu status para "alugado"
 */
async function alugarArmario(idUnidade: string) {
  let res;

  // Caso não conste o parametro esperado
  if (!idUnidade) {
    functions.logger.error("confirmarLoc " +
        "- Erro ao confirmar locacao: Dados não recebidos corretamente"),
    res = {
      status: "ERROR",
      message: "Parâmetros não recebidos corretamente",
      payload: {docId: null},
    };
  }
  try {
  // Referenciar unidade de locação e collection de armários daquela unidade
    const unitRef = colUnidades.doc(idUnidade);
    const colArmarios = unitRef.collection("armarios");

    // Buscar armario que esteja disponivel
    const lockerSnapshot = await colArmarios.where("status", "==",
      "disponivel").limit(1).get();

    // Verificar se o documento do armario existe e se o status é "disponível"
    if (!lockerSnapshot.empty) {
      // acessar primeiro documento de armário retornado pela busca
      const armario = lockerSnapshot.docs[0];
      // atualiza status do armário
      await armario.ref.update({status: "alugado"});

      functions.logger
        .info("Sucesso - Status do armário atualizado para 'alugado'.");

      res = {
        status: "SUCCESS",
        message: "Armário disponível encontrado e atualizado.",
        payload: {
          idArmario: armario.id,
          data: armario.data(),
        },
      };
    } else {
      functions.logger
        .info("Não há armários disponíveis no momento.");

      res = {
        status: "ERROR",
        message: "Não há armários disponíveis no momento.",
        payload: {
          idArmario: null,
          data: null,
        },
      };
    }
  } catch (error: any) {
    res = {
      status: "ERROR",
      message: "Erro ao buscar armário: " + error.message,
      payload: {
        idArmario: null,
        data: null,
      },
    };
    functions.logger.error("atualizarArmario - Erro ao buscar armário: " +
     error.message);
  }
  return res;
}

/**
 * Função que busca o documento de uma locação via número da tag fornecido
 * documento será encontrado caso idTag esteja presente no campo de array
 * "tags"
 * @param idTag - identificação da pulseira nfc, campo presente no documento de
 * locação
 */
export const buscarLoc = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    const idTag = data.idTag;

    functions.logger.info("Function buscarLoc - iniciada.");

    if (!idTag) {
      functions.logger.error("buscarLoc "+
      "- Erro: Dados não recebidos corretamente");
      result = {
        status: "ERROR",
        message: "Erro ao receber parâmetros.",
        payload: JSON.parse(JSON.stringify({
          idLocacao: null,
          data: null})),
      };
    }
    try {
      /* Buscar na collection "locação" documento que contenha
      o id da tag recebida dentro do array */
      const locSnapshot = await colLocacao
        .where("tags", "array-contains", idTag).limit(1).get();

      // Caso a busca não retorne vazia
      if (!locSnapshot.empty) {
        // Sucesso - Atribui 1o resultado da busca à variável
        const locacao =locSnapshot.docs[0];

        result = {
          status: "SUCCESS",
          message: "Locacao encontrada via tag.",
          payload: JSON.parse(JSON.stringify({
            idLocacao: locacao.id,
            data: locacao.data()})),
        };
        functions.logger.info("buscarLoc " +
         "- Locação encontrada com sucesso.");
        functions.logger.info("Function buscarLoc - finalizando.");
      } else {
        result = {
          status: "ERROR",
          message: "Locação não encontrada ou não existe.",
          payload: JSON.parse(JSON.stringify({
            idLocacao: null,
            data: null})),
        };
        throw new functions.https.HttpsError("not-found", result.message);
      }
    } catch (error: any) {
      // Lidar com erros
      functions.logger.error("buscarLoc" + "Erro ao buscar locação: " +
       error.message);

      result = {
        status: "ERROR",
        message: "Erro ao buscar locação." + error.message,
        payload: JSON.parse(JSON.stringify({
          idLocacao: null,
          data: null})),
      };

      throw new functions.https.HttpsError("internal", result.message);
    }
    return result;
  });

/**
 * Função que exclui um documento de locação do firebase db e chama outra
 * função para atualizar status do armário alugado.
 * @param {string} idLocacao - id da locação que deseja encerrar
 * @param {string} idUnidade - id da unidade onde está feita a locação
 * @todo - nao está pegando o idArmario !!
 */
export const encerrarLoc = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result;

    // Extraindo dados da request
    const idLocacao = data.idLocacao;
    const idUnidade = data.idUnidade;


    functions.logger.info("Function encerrarLoc - iniciada.");
    // Caso ausência do dado idLocacao, preparar resposta de erro
    if (!idLocacao || !idUnidade) {
      functions.logger.error("confirmarLoc " +
      "- Erro ao encerrar locacao: Dados não recebidos corretamente");
      throw new functions.https
        .HttpsError("invalid-argument", "Parametros inválidos.");
    }
    try {
      // Referenciando documento de locação do banco
      const locRef = colLocacao.doc(idLocacao);
      const locSnapshot = await locRef.get();

      // Obtendo campo "armario" do documento de locação
      const armarioPath = locSnapshot.get("armario");

      if (!armarioPath) {
        throw new functions.https.HttpsError("invalid-argument",
          "Referência do armário não encontrada no documento de locação.");
      }
      // Transformando o path para pegar o id do armario referenciado
      const idArmario = armarioPath.id;

      // Atualizando status do armário de "alugado" para "disponivel"
      await liberarArmario(idUnidade, idArmario);

      // Método para deletar documento
      await locRef.delete();

      result = {
        status: "SUCCESS",
        message: "Locação encerrada com sucesso: documento deletado",
      };

      functions.logger.info("encerrarLoc " + "- " + result.message);
    } catch (error: any) {
      result = {
        status: "ERROR",
        message: "Parâmetros não recebidos corretamente",
        payload: JSON.parse(JSON.stringify({
          idLocacao: idLocacao,
          idUnidade: idUnidade,
        })),
      };
    }
    return result;
  });

/**
 * Função que volta o armário para o status "disponível"
 * @param {string} idArmario - id do armario que está presente no doc de locação
 * @param {string} idUnidade - id da unidade de locação onde o armário está
 *  presente
 * */
async function liberarArmario(idUnidade:string, idArmario: string) {
  let res;

  functions.logger.info("Function liberarArmario - iniciada.");
  // Caso não conste o parametro esperado
  if (!idArmario) {
    functions.logger.error("confirmarLoc " +
          "- Erro ao liberar armario: Dados não recebidos corretamente " +
           idArmario),
    res = {
      status: "ERROR",
      message: "Parâmetros não recebidos corretamente",
    };
    throw new functions.https.HttpsError("invalid-argument", res.message);
  }
  try {
    // referenciando documentos do bd e collections
    const unitRef = colUnidades.doc(idUnidade); // doc UnidadeLocacao
    const colArmarios = unitRef.collection("armarios");
    const armarioRef = colArmarios.doc(idArmario); // doc armario

    // Atualizando campo 'status' do armário para 'disponivel'
    await armarioRef.update({status: "disponivel"});

    res = {
      status: "SUCCESS",
      message: "Armário liberado com sucesso.",
      payload: {
        idArmario: armarioRef,
      },
    };

    functions.logger.info("liberarArmario " + res.message);
  } catch (error:any) {
    // Lidar com erros e preparar resposta de retorno
    functions.logger.error("Erro ao buscar ou atualizar armário.", +
    error.message);
    res = {
      status: "ERROR",
      message: "Erro ao buscar armário.",
    };
    throw new functions.https.HttpsError("not-found",
      "Documento não encontrado.");
  }
  return res;
}

