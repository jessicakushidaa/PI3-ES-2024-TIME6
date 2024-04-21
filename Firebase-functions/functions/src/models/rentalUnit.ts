/* eslint-disable linebreak-style */
import {Pessoa} from "../models/user";

interface Coordenadas {
    latitude: number,
    longitude: number
}

interface Armario {
    tagArmario: string,
    situacao: string
}

interface PrecoTempo {
    preco: number,
    tempo: number
}

// Define a estrutura de dados de uma unidade de locacao
export interface UnidadeLocacao {
    id: string,
    nome: string,
    coordenadas: Coordenadas,
    endereco: string,
    descricao: string,
    gerentes: Pessoa[],
    telefone: string,
    armarios: Armario[],
    tabelaPrecos: PrecoTempo[]
  }
