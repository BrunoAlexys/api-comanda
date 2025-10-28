📡 API de Comandas para Restaurantes

<div align="center"> <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"> <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"> <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis"> <img src="https://img.shields.io/badge/Tests-JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5"> </div>

⚠️ Projeto de Portfólio | Repositório da API (Backend) para o sistema de gerenciamento de comandas de restaurantes. Esta API é o cérebro por trás da operação, servindo o aplicativo mobile dos garçons.

Status do Projeto

O projeto está em desenvolvimento ativo. A funcionalidade de autenticação (via Spring Security) está 100% funcional, permitindo que o aplicativo mobile valide as credenciais do garçom e obtenha um token de acesso.

🎯 Sobre o Projeto

Este projeto é uma API RESTful robusta que gerencia toda a lógica de negócios, persistência de dados e segurança do sistema de comandas. Ele é responsável por processar os pedidos, gerenciar mesas, autenticar usuários e fornecer os dados necessários para o frontend.

Este repositório contém exclusivamente o projeto backend (API).

🔗 Projeto Mobile (Frontend)

O frontend deste sistema (aplicativo Flutter) foi desenvolvido para ser a ferramenta de trabalho do garçom e pode ser encontrado em outro repositório.

Link do App Mobile: https://github.com/BrunoAlexys/mobile-comanda.git

✨ Funcionalidades Atuais

✅ API de Autenticação: Endpoints seguros (/login) usando Spring Security para validar credenciais e emitir tokens (ex: JWT).

✅ Infraestrutura com Docker: Ambiente de desenvolvimento totalmente containerizado com Docker Compose, gerenciando os serviços de PostgreSQL e Redis.

✅ Testes Unitários: Cobertura de testes para a camada de serviços e controladores, garantindo a lógica de negócio.

🗺️ Roadmap (Próximos Passos)

▶️ CRUD de Produtos: Gerenciamento do cardápio (itens, preços, categorias).

▶️ Gestão de Mesas: Lógica para abrir, fechar e visualizar o status das mesas.

▶️ Lançamento de Pedidos: Endpoints para criar pedidos, associar itens a uma comanda/mesa.

▶️ Gerenciamento de Comanda: Adicionar, remover ou editar itens de um pedido em aberto.

▶️ Fechamento de Conta: Cálculo de total, divisão de conta e integração com pagamentos.

🛠️ Stack Tecnológica

Este projeto foi construído utilizando um stack moderno, escalável e performático para o backend.

Backend (Este Repositório)

Java 21: Utilizando a versão mais recente do Java para aproveitar seus recursos modernos e melhorias de performance.

Spring Boot 3.x: Framework principal para a criação rápida e robusta de APIs RESTful.

Spring Security: Gerenciamento completo de autenticação e autorização dos endpoints.

Spring Data JPA: Para persistência de dados de forma simplificada com o banco de dados.

PostgreSQL: Banco de dados relacional principal para armazenar dados de pedidos, usuários, mesas, etc.

Redis: Banco de dados em memória de alta performance, utilizado para cache (ex: cache do cardápio) ou gerenciamento de sessão.

JUnit 5 & Mockito: Ferramentas para a criação de testes unitários e de integração, garantindo a qualidade e estabilidade da API.

Infraestrutura

Docker & Docker Compose: O projeto utiliza Docker para criar um ambiente de desenvolvimento padronizado. O docker-compose.yml orquestra os contêineres do PostgreSQL e do Redis, garantindo que os serviços de banco de dados e cache estejam rodando com um único comando, de forma isolada e consistente.

🏛️ Arquitetura e Conceitos Aplicados

A API segue as melhores práticas de design de software para garantir manutenibilidade e escalabilidade.

Arquitetura em Camadas (N-Tier): Separação clara de responsabilidades (Controller, Service, Repository) para facilitar os testes e a manutenção.

API RESTful: Design de endpoints seguindo os padrões e verbos HTTP (GET, POST, PUT, DELETE).

DTO (Data Transfer Object): Utilização de DTOs para desacoplar as entidades do banco de dados dos contratos da API, evitando exposição de dados sensíveis.

Injeção de Dependência (DI): Princípio fundamental do Spring, usado para gerenciar os componentes e facilitar os testes (mocking).

🧪 Qualidade e Testes

A qualidade do código é garantida através de uma suíte de testes robusta.

Testes Unitários (JUnit 5 + Mockito): Focamos em testar as regras de negócio na camada de Serviço (Service) de forma isolada. Mockamos as dependências (como os Repositórios) para validar que a lógica de negócio se comporta como esperado em diferentes cenários (sucesso, erro, exceções).

Testes de Integração: (Roadmap) Testes que validam a interação entre as camadas, incluindo a comunicação real com o banco de dados (PostgreSQL) para garantir a integridade dos dados.

🚀 Como Executar o Projeto

Pré-requisitos:

Ter o JDK 21 (ou superior) instalado.

Ter o Docker e o Docker Compose instalados.

Ter o Apache Maven instalado.

Clone o repositório:

Bash

git clone https://github.com/BrunoAlexys/api-comanda.git
cd api-comanda
Configure o ambiente: (Recomendado) Copie o arquivo application.properties.example para application.properties ou application-dev.properties e ajuste as variáveis de banco de dados, Redis e segredos do JWT, se necessário.

Suba a infraestrutura (Banco de Dados e Cache): Este comando irá iniciar os contêineres do PostgreSQL e do Redis em background.

Bash

docker-compose up -d
Execute a aplicação Spring Boot:

Bash

mvn spring-boot:run
Alternativamente, se estiver usando o wrapper do Maven:

Bash

./mvnw spring-boot:run
Acesso: A API estará disponível em http://localhost:8080.

(Opcional) Documentação da API: Se o SpringDoc (Swagger) estiver configurado, a documentação dos endpoints estará disponível em: http://localhost:8080/swagger-ui.html

👨‍💻 Autores

Este projeto é mantido por dois desenvolvedores como parte de nosso portfólio. Sinta-se à vontade para entrar em contato!

<table align="center" border="0" cellpadding="10" cellspacing="0" style="border: none !important;"> <tr style="border: none !important;"> <td align="center" style="border: none !important;"> <a href="https://github.com/BrunoAlexys"> <img src="https://github.com/BrunoAlexys.png" width="100px;" alt="Foto do Bruno Alexys"/> <br /> <sub><b>Bruno Álexys</b></sub> </a> <br /> <a href="https://www.linkedin.com/in/bruno-alexys-moura/">LinkedIn</a> | <a href="https://github.com/BrunoAlexys">GitHub</a> </td> <td align="center" style="border: none !important;"> <a href="https://github.com/BrenoMoura00"> <img src="https://github.com/BrenoMoura00.png" width="100px;" alt="Foto de Breno Moura"/> <br /> <sub><b>Breno Moura</b></sub> </a> <br /> <a href="https://www.linkedin.com/in/breno-moura-silva/">LinkedIn</a> | <a href="https://github.com/BrenoMoura00">GitHub</a> </td> </tr> </table>
