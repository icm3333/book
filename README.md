# 📚 Book Tracker API

API REST desenvolvida em **Java** com **Spring Boot** para gerenciar o progresso de leitura de livros. O diferencial deste projeto é a integração inteligente com a **Open Library API**, permitindo que o usuário informe apenas o título e o sistema busque automaticamente o autor e o número total de páginas antes de persistir os dados no banco.

---

## 🚀 Tecnologias Utilizadas

* **Java 17+**
* **Spring Boot**
* **Spring Data JPA**
* **PostgreSQL**
* **Lombok**
* **RestTemplate** (Consumo de API externa)

---

## 🛠️ Arquitetura e Funcionalidades

O projeto segue uma arquitetura em camadas bem definida:
* **Controller:** Expõe os endpoints REST para o cliente.
* **Service:** Contém a regra de negócio e orquestra a comunicação com a API externa.
* **Client:** Faz a requisição HTTP para a Open Library.
* **Repository:** Interface do Spring Data JPA para comunicação com o PostgreSQL.

### Endpoints Disponíveis:
* `GET /v1/api/books` - Lista todos os livros salvos.
* `POST /v1/api/books` - Cadastra um novo livro buscando os dados automaticamente na Open Library.
* `DELETE /v1/api/books/{id}` - Remove um livro pelo ID.

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos
* Java 17 ou superior instalado.
* PostgreSQL rodando localmente.
* Maven instalado.

### 1. Clonar o Repositório
```bash
git clone [https://github.com/seu-usuario/book-tracker.git](https://github.com/seu-usuario/book-tracker.git)
cd book-tracker