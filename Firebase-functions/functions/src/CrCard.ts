import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

const app = admin.initializeApp();
const db = app.firestore();
const colCartoes = db.collection("cartoes");

interface CallableResponse {
    status: string;
    message: string;
    payload: any; // Use any para o payload, pois pode ser de qualquer tipo
}

interface Cartao {
    titularName: string;
    cardNumber: string;
    cvv: number;
    dataVal: Date;
}

function camposPreenchidos(p: Cartao): number {
    if (!p.titularName) {
        return 1;
    }
    if (!p.cardNumber) {
        return 2;
    }
    if (!p.cvv) {
        return 3;
    }
    if (!p.dataVal) {
        return 4;
    }
    return 0;
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

    return camposInvalidos.length > 0 ? camposInvalidos : null;
}

function errorMessage(codigo: number): string {
    switch (codigo) {
        case 1:
            return "Nome do titular do cartão não informado.";
        case 2:
            return "Número do cartão não informado.";
        case 3:
            return "CVV não informado";
        case 4:
            return "Data de validade não informado";
        default:
            return "";
    }
}

export const addCartao = functions
    .region("southamerica-east1")
    .https.onCall(async (data, context) => {
        const cartao: Cartao = {
            titularName: data.titularName,
            cardNumber: data.cardNumber,
            cvv: data.cvv,
            dataVal: new Date(data.dataVal),
        };

        const camposInvalidos = validarTiposCartao(cartao);
        const erroMensagem = errorMessage(camposInvalidos ? camposInvalidos[0] : 0);

        if (camposInvalidos) {
            functions.logger.error("addCartao - Erro ao inserir novo Cartão: " + erroMensagem);
            return {
                status: "ERROR",
                message: erroMensagem,
                payload: null,
            };
        } else {
            const docRef = await colCartoes.add(cartao);
            functions.logger.info("addCartao - Novo cartão inserido");
            return {
                status: "SUCCESS",
                message: "Cartão inserido com sucesso.",
                payload: { cardId: docRef.id },
            };
        }
    });

export const getCartaoById = functions
    .region("southamerica-east1")
    .https.onCall(async (data, context) => {
        try {
            const cardId: string = data.cardId;
            const cardRef = colCartoes.doc(cardId);
            const cardSnapshot = await cardRef.get();

            if (!cardSnapshot.exists) {
                return {
                    status: "ERROR",
                    message: "Cartão não encontrado",
                    payload: { cardSnapshot: null },
                };
            } else {
                return {
                    status: "SUCCESS",
                    message: "Cartão recuperado com sucesso",
                    payload: { cartao: cardSnapshot.data() },
                };
            }
        } catch (error: any) {
            return {
                status: "ERROR",
                message: "Erro ao obter cartão: " + error.message,
                payload: { cardSnapshot: null },
            };
        }
    });
