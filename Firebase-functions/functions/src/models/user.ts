/* eslint-disable linebreak-style */

/* Tipando os atributos do objeto Pessoa que será inserido na collection */
// campo "userId" vem como parametro no lado do cliente, id do usuário
// autenticado
export interface Pessoa {
    userId: string,
    nome: string,
    sobrenome: string,
    dataNascimento: Date,
    cpf: string,
    telefone: string,
    isGerente: boolean
  }
