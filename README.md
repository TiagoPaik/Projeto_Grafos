# Projeto Grafos — Roteamento Urbano com Menor Caminho

> Solução computacional para descoberta de rotas otimizadas em ambientes urbanos, utilizando algoritmos de busca em grafos para calcular o menor caminho com base em distância e tempo de percurso.

---

## Situação-Problema

O crescimento acelerado das cidades modernas torna o planejamento eficiente de rotas um desafio crítico para a mobilidade urbana. Identificar o menor caminho entre dois pontos de interesse — levando em conta distância percorrida e tempo estimado — é um problema clássico da Teoria dos Grafos com aplicações diretas em navegação, logística e transporte público.

Este projeto propõe uma solução interativa e visual para esse problema, oferecendo **dois modos de entrada**: um mapa urbano gerado proceduralmente em grade 100×100, com pontos de interesse categorizados (casas, escolas, hospitais, faculdades e mercados); e um modo de carregamento via arquivo `.txt`, suportando grafos representados por **lista de adjacência** (Formato A) ou **matriz de adjacência** (Formato B). Em ambos os modos, os algoritmos BFS e DFS percorrem o grafo e calculam o caminho mínimo, exibindo distância em metros/quilômetros e tempo em segundos/minutos.

---

## Tecnologias Utilizadas

| Tecnologia | Versão | Descrição |
|---|---|---|
| Java | 17 | Linguagem principal do projeto |
| JavaFX | 22.0.2 | Interface gráfica e visualização interativa |
| Maven | 3.x | Gerenciamento de dependências e build |
| JavaFX Maven Plugin | 0.0.8 | Execução facilitada via Maven |

---

## Arquitetura do Projeto

```
src/main/java/
├── app/
│   ├── MainApp.java          # Ponto de entrada JavaFX — modo Cidade 100×100
│   ├── MenuView.java         # Menu principal (seleção de modo e algoritmo)
│   ├── GraphView.java        # Visualizador de grafos genéricos (Formato A/B)
│   └── BFSVisualizer.java    # Visualizador auxiliar BFS
│
├── algo/
│   ├── SearchAlgorithm.java  # Interface comum para algoritmos de busca
│   ├── BfsAlgorithm.java     # Implementação do BFS em grid
│   ├── DfsAlgorithm.java     # Implementação do DFS em grid
│   ├── GraphSearch.java      # BFS/DFS para grafos genéricos
│   ├── GraphAnalyzer.java    # Análise completa do grafo (relatório)
│   └── SearchStep.java       # Estrutura de dados de um passo da busca
│
├── model/
│   ├── Graph.java            # Modelo de grafo genérico (direcionado/não-direcionado, ponderado)
│   ├── GridMap.java          # Mapa em grade com cálculo de distância e tempo por aresta
│   ├── Cell.java             # Célula do grid (linha, coluna)
│   ├── Poi.java              # Ponto de interesse urbano
│   ├── PoiRegistry.java      # Registro de todos os POIs do mapa
│   └── PoiType.java          # Enum de tipos de POI (CASA, ESCOLA, HOSPITAL, FACULDADE, MERCADO)
│
└── util/
    ├── CityGridGenerator.java # Geração procedural da cidade 100×100
    ├── GraphParser.java       # Parser dos Formatos A e B a partir de .txt
    └── PoiGenerator.java      # Geração de POIs sobre o grid urbano
```

---

## Funcionalidades

### Modo 1 — Cidade 100×100 (Mapa Grid)

- Cidade gerada proceduralmente com seed fixa, garantindo reprodutibilidade
- Grade de 100×100 células com paredes e corredores livres
- Pontos de interesse (POIs) categorizados por tipo e posicionados sobre o grid
- Filtro visual por categoria de POI (Casa, Escola, Hospital, Faculdade, Mercado)
- Seleção interativa de destino via lista suspensa ou clique direto na célula
- Execução animada de **BFS** e **DFS** com controle de velocidade
- Cálculo e exibição do caminho mínimo com:
  - Número de hops (arestas percorridas)
  - Distância total em metros e quilômetros
  - Tempo total em segundos e minutos
- Relatório textual completo ao término da busca

### Modo 2 — Carregamento de Arquivo `.txt`

- Suporte a dois formatos de entrada:

  **Formato A — Lista de Adjacência**
  ```
  n m tipo        (tipo: D = Direcionado, U = Não-direcionado)
  u v [w]         (arestas, peso opcional)
  ...
  ```

  **Formato B — Matriz de Adjacência**
  ```
  n tipo
  (matriz n × n com valores 0/1 ou pesos)
  ```

- Detecção automática do formato pela quantidade de tokens na primeira linha
- Suporte a grafos ponderados e não-ponderados
- Visualização gráfica circular (até 60 vértices) ou em grade (acima de 60)
- Animação de BFS e DFS com coloração de vértices visitados, fronteira e caminho
- Relatório de análise com: grau dos vértices, conectividade, caminho s→t, vizinhança por nível k

---

## Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.6+

### Build e Execução

```bash
# Clonar o repositório
git clone https://github.com/TiagoPaik/Projeto_Grafos.git
cd Projeto_Grafos/BFS

# Executar via Maven (recomendado)
mvn javafx:run

# Ou compilar o projeto
mvn clean package
```

---


## Algoritmos Implementados

### BFS — Busca em Largura
Explora o grafo nível a nível, garantindo que o **primeiro caminho encontrado seja o de menor número de hops**. Utiliza uma fila (`Deque`) para controle da fronteira. Ideal para encontrar o caminho mínimo em grafos não-ponderados.

### DFS — Busca em Profundidade
Explora o grafo seguindo um único caminho até o fim antes de retroceder. Utiliza uma pilha (`Deque`) para controle da fronteira. Não garante o caminho mínimo, mas é útil para detectar conectividade e explorar estruturas do grafo.
