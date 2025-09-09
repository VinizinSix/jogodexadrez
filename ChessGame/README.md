Jogo de Xadrez com IA (Java Swing)
Este é um projeto simples de um jogo de xadrez com um adversário de inteligência artificial. O jogo foi desenvolvido usando a biblioteca Java Swing para a interface gráfica, e a IA utiliza o algoritmo Minimax para tomar decisões.

Funcionalidades
Jogo de xadrez para um jogador contra o computador.

Destaque visual dos movimentos legais.

Destaque das casas do último movimento e do rei em xeque.

IA com níveis de dificuldade (profundidade de busca) ajustáveis.

Mensagem de xeque, xeque-mate e empate.

A Inteligência Artificial
A IA deste jogo foi construída com base em conceitos clássicos de inteligência artificial para jogos:

Algoritmo Minimax com Poda Alpha-Beta: A IA simula possíveis sequências de movimentos para frente, avaliando o resultado de cada uma. A poda Alpha-Beta a ajuda a "podar" galhos de lances que são desvantajosos, tornando a análise mais rápida e profunda.

Função de Avaliação: Cada posição do tabuleiro é avaliada e recebe uma pontuação. A pontuação é baseada no valor das peças no tabuleiro e em sua posição. Por exemplo, controlar o centro do tabuleiro aumenta a pontuação. A IA busca sempre maximizar sua pontuação.

Livro de Aberturas: Para os primeiros lances do jogo, a IA utiliza um "livro de aberturas", uma base de dados de lances pré-determinados que são considerados aberturas padrão no xadrez.

Como Executar
Para rodar este projeto, você precisa ter o Java Development Kit (JDK) instalado em seu computador.

Clone o repositório:

git clone [https://seu-repositorio-aqui.git](https://seu-repositorio-aqui.git)
cd seu-repositorio-aqui

Compile o código:
Abra o terminal na pasta do projeto e execute o seguinte comando:

javac src/main/java/view/ChessGame.java

Nota: Se a sua estrutura de pastas for diferente, ajuste o caminho do arquivo.

Execute o jogo:

java -cp src/main/java/ view.ChessGame

Nota: Se a sua estrutura de pastas for diferente, ajuste o caminho.

Screenshot
!

Próximos Passos (Possíveis Melhorias)
Implementar as regras de roque e en passant.

Adicionar uma interface gráfica mais moderna, talvez com imagens para as peças.

Criar um histórico de lances mais detalhado.

Adicionar a funcionalidade de um jogo de dois jogadores.

Licença
Este projeto é de código aberto e está disponível sob a licença MIT.