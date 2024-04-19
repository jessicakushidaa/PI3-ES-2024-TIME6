/* eslint-disable linebreak-style */
import * as admin from "firebase-admin";

admin.initializeApp();

// Importar funções e interfaces
import {addPessoa} from "./controllers/userController";
import {getDocumentId, getDocumentFields} from
  "./controllers/documentController";
import {addCartao} from "./controllers/cardController";
import {getAllUnits, addUnidadeLocacao}
  from "./controllers/rentalUnityController";
import {CallableResponse} from "./models/customResponse";
import * as user from "./models/user";
import * as card from "./models/card";
import * as rentalUnit from "./models/rentalUnit";

// Exportar as funções e interfaces
export {addPessoa, getDocumentId, getDocumentFields, addCartao, getAllUnits,
  addUnidadeLocacao};
export {CallableResponse, user, card, rentalUnit};

