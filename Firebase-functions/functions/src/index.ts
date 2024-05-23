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
import {addLocacao, checkLocacao, confirmarLoc, buscarLoc, encerrarLoc,
  buscarConfirmadas} from "./controllers/rentController";
import {CallableResponse} from "./models/customResponse";
import * as user from "./models/user";
import * as card from "./models/card";
import * as rentalUnit from "./models/rentalUnit";
import * as rent from "./models/rent";

// Exportar as funções e interfaces
export {addPessoa, getDocumentId, getDocumentFields, addCartao, getAllUnits,
  addUnidadeLocacao, addLocacao, checkLocacao, confirmarLoc, buscarLoc,
  encerrarLoc, buscarConfirmadas};
export {CallableResponse, user, card, rentalUnit, rent};

