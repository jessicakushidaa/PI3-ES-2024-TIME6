import * as functions from "firebase-functions";
import * as admin from "firebase-admin";


const app = admin.initializeApp();
const db = app.firestore();
const colCartoes = db.collection("cartoes")

interface CallableResponse {
    status: string,
    message: string,
    payload: JSON
  }

interface Cartao{
    titularName: string,
    cardNumber: string,
    cvv: number,
    dataVal: Date;
}

function camposPreenchidos(p: Cartao){
    if (!p.titularName){
        return 1;
    }
    if (!p.cardNumber){
        return 2;
    }
    if (!p.cvv){
        return 3;
    }
    if (!p.dataVal){
        return 4;
    }
    else{
        return 0;
    }
}

function validarTiposCartao(cartao: Cartao): string[] | null {
    const camposInvalidos: string[] = [];

    if (typeof cartao.titularName !== "string") {
        camposInvalidos.push("titularName");
    }
    if (typeof cartao.cardNumber !== "string") {
        camposInvalidos.push("cardNumber");
    }
    if (typeof cartao.cvv !== "number") {
        camposInvalidos.push("cvv");
    }
    if (!(cartao.dataVal instanceof Date)) {
        camposInvalidos.push("dataVal");
    }

    if (camposInvalidos.length > 0) {
        return camposInvalidos;
    }

    return null;
}

function ErrorMensage(codigo: number){
    let message = "";
    switch (codigo) {
        case 1{
            message = "Nome do titular do cartão não informado.";
            break;
        }
        case 2{
            message = "Número do cartão não informado.";
            break;
        }
        case 3{
            message = "CVV não informado";
            break;
        }
        case 4{
            message = "Data de validade não informado";
            break;
        }
    }
    return message;
}

export const addCartao = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
    let result: CallableResponse;

    functions.logger.info("Function addCartao - Iniciada.");

    const cartao: Cartao = {
      titularName: data.titularName,
      cardNumber: data.cardNumber,
      cvv: data.cvv,
      dataVal: new Date(data.dataVal),
    };

    const camposInvalidos = validarTiposcartao(cartao);
    const erroMensagem = ErrorMensage(camposInvalidos ? camposInvalidos[0] : 0);

    if (camposInvalidos) {
      functions.logger.error("addCartao " +
        "- Erro ao inserir novo Cartão:" +
        erroMensagem);

      result = {
        status: "ERROR",
        message: erroMensagem,
        payload: null,
      };
    } else {
      const docRef = await colCartoes.add(cartao);
      result = {
        status: "SUCCESS",
        message: "Cartão inserido com sucesso.",
        payload: { cardId: docRef.id },
      };
      functions.logger.info("addCartao - Novo cartão inserido");
    }

    return result;
  });


  export const getCartaoById = functions
  .region("southamerica-east1")
  .https.onCall(async (data, context) => {
      let result: CallableResponse;
      try {
          const cardId: string = data.cardId;
          const cardRef = colCartoes.doc(cardId);
          const cardSnapshot = await cardRef.get();

          if (!cardSnapshot.exists) {
              result = {
                  status: "ERROR",
                  message: "Cartão não encontrado",
                  payload: JSON.parse(JSON.stringify({ cardSnapshot: null })),
              };
          } else {
              result = {
                  status: "SUCCESS",
                  message: "Cartão recuperado com sucesso",
                  payload: JSON.parse(JSON.stringify({ cartao: cardSnapshot.data() })),
              };
          }
      } catch (error: any) {
          result = {
              status: "ERROR",
              message: "Erro ao obter cartão: " + error.message,
              payload: JSON.parse(JSON.stringify({ cardSnapshot: null })),
          };
      }
      return result;
  });