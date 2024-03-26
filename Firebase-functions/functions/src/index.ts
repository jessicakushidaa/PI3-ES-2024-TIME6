// importar as funcionalidades do firebase functions, apelidando de 'functions';
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// OBJETOS CONSTANTES

// criar objetos const fora da function pois vai usar em várias
// const app recebe o retorno do método "initializeApp" de admin
// inicializa o app do firebase
const app = admin.initializeApp();

const db = app.firestore(); // chamada do banco de dados


/* tipando os atributos do objeto que será inserido na collection */
interface Pessoa {
  idPessoa?: string,
  nome: string,
  sobrenome: string,
  dataNascimento: Date,
  telefone: string,
  //senha: string
  isGerente?: boolean,
  cpfUsuario: string
}

interface Gerente{
  unidadeLocacao: UnidadeLocacao
}

/*
interface Cliente extends Pessoa {
  cartaoCredito?: CartaoCredito;
  armarioAlocado?: Locacao[]
}

interface CartaoCredito{
  nomeCartao: string,
  numero: number,
  dataValidade: Date
}
*/
interface UnidadeLocacao {
  id: string | null,
  endereco: string,
  gerenteResponsavel: Gerente,
  telefone: string,
  armarios: Armario[],
  tabelaPrecos: PrecoTempo[]
}

interface PrecoTempo{
  idDuracao: number,
  tempo: number,
  preco: number
}

// id do armario deve ser padrao; ex: loc-001, loc-002
interface Armario {
  idArmario: string,
  situacao: string
}

// collection do firestore, caso nao tenha, ele cria;
const colPessoas = db.collection("pessoas");

export const addPessoa = functions
  .region("southamerica-east1")
  .https.onRequest(async (req, res) => {
    try {
      // Extrair os dados da pessoa do corpo da request
      const {idPessoa, nome, sobrenome, dataNascimento, telefone, cpfUsuario}:
      Pessoa = req.body;

      // Criar objeto pessoa com os dados extraídos
      const pessoa:
      Pessoa ={idPessoa, nome, sobrenome, dataNascimento, telefone, cpfUsuario};

      // Adicionar a pessoa à coleção 'pessoas' no Firestore
      const docRef = await colPessoas.add(pessoa);

      // resposta caso sucesso
      // res.status(200).send(`Pessoa inserida. Referência: ${docRef.id}`);

      // Atualizar o ID da pessoa com o ID do documento criado
      // teste : await docRef.update({idPessoa: docRef.id});
      pessoa.idPessoa = docRef.id;
      // alterar mensagem para ficar mais bonita
      res.status(200).send("id alterado. Referencia ID" + pessoa.nome +
      pessoa.idPessoa);
    } catch (error) {
      // Lidar com erros
      console.error("Erro ao inserir pessoa:", error);
      res.status(500).send("Erro ao inserir pessoa.");
    }
  });
// const colLockets = db.collection("lockets");
