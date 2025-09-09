# ♟️ Jogo de Xadrez com IA (Java Swing)



Este é um **jogo de xadrez para um jogador contra o computador**, desenvolvido em **Java** usando **Java Swing**.  
A IA utiliza conceitos de **Minimax** com **poda Alpha-Beta** para tomar decisões estratégicas.

---

## 🎯 Funcionalidades

- Jogo de xadrez contra o computador.
- Destaque visual de **movimentos legais**.
- Realce do **último lance** e do **rei em xeque**.
- IA com **níveis de dificuldade ajustáveis** (profundidade de busca).
- Mensagens automáticas de **xeque**, **xeque-mate** e **empate**.

---

## 🤖 Inteligência Artificial

A IA do jogo foi construída com base em princípios clássicos de IA para jogos de tabuleiro:

- **Minimax com Poda Alpha-Beta**  
  Simula possíveis sequências de lances, descartando aqueles que não são vantajosos, tornando a análise mais rápida.

- **Função de Avaliação**  
  Cada posição no tabuleiro recebe pontuação com base no **valor das peças** e **posicionamento estratégico** (ex.: controle do centro do tabuleiro).

- **Livro de Aberturas**  
  Nos primeiros lances, a IA usa um conjunto de aberturas padrão do xadrez, garantindo um início sólido.

---

## ⚙️ Como Executar

### Pré-requisitos

- **Java Development Kit (JDK)** instalado.

