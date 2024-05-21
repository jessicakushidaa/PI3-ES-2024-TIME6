/* eslint-disable linebreak-style */
import {UnidadeLocacao, PrecoTempo} from "./rentalUnit";
import {Pessoa} from "./user";

export interface Locacao {
    armario: UnidadeLocacao["armarios"],
    cliente: Pessoa[],
    precoTempoEscolhido: PrecoTempo
    status: string
    foto: string[]
}
